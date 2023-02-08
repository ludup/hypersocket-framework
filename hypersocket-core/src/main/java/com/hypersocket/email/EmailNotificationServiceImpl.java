package com.hypersocket.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.simplejavamail.MailException;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.messagedelivery.MessageDeliveryException;
import com.hypersocket.messagedelivery.MessageDeliveryResult;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.ServerResolver;
import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.utils.HttpUtils;

@Service
public class EmailNotificationServiceImpl extends AbstractAuthenticatedServiceImpl implements EmailNotificationService {

	private final static List<String> NO_REPLY_ADDRESSES = Arrays.asList("noreply", "no.reply", "no-reply", "no_reply",
			"do_not_reply", "do.not.reply", "do_not_reply");

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private EmailTrackerService trackerService;

	@Autowired
	private EventService eventService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private MailerService mailerService;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private HttpUtils httpUtils;

	@Autowired
	private MessageDeliveryService messageDeliveryService;

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

//	ThreadLocal<Boolean> synchronousEmail = new ThreadLocal<>();

	public final static String SMTP_DO_NOT_SEND_TO_NO_REPLY = "smtp.doNotSendToNoReply";

	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	public static final String OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX = "OGIAU";

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		messageDeliveryService.registerProvider(this);
		ThreadLocal.withInitial(() -> (Boolean.FALSE));
	}

	@Override
	@SafeVarargs
	@Deprecated
	public final void sendEmail(String subject, String text, String html, RecipientHolder[] recipients, boolean archive,
			boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException {
		sendEmail(getCurrentRealm(), subject, text, html, recipients, archive, track, delay, context, attachments);
	}

	@Override
	@SafeVarargs
	@Deprecated
	public final void sendEmail(Realm realm, String subject, String text, String html, RecipientHolder[] recipients,
			boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException {
		sendEmail(realm, subject, text, html, null, null, recipients, archive, track, delay, context, attachments);
	}

	@Override
	public MediaType getSupportedMedia() {
		return MediaType.EMAIL;
	}

	@Override
	public String getResourceKey() {
		return "SMTP";
	}

	@Override
	public EmailNotificationBuilder newBuilder(Realm realm) {
		var b = new EmailNotificationBuilder() {
			@Override
			protected MessageDeliveryResult sendImpl() throws MessageDeliveryException {
				return EmailNotificationServiceImpl.this.send(this);
			}

			@Override
			public RecipientHolder parseRecipient(String recipientAddress) throws ValidationException {
				return EmailNotificationServiceImpl.this.parseRecipient(recipientAddress);
			}
		};
		b.realm(realm == null ? getCurrentRealm() : realm);
		return b;
	}

	@Override
	@SafeVarargs
	@Deprecated
	public final void sendEmail(Realm realm, String recipeintSubject, String receipientText, String receipientHtml,
			String replyToName, String replyToEmail, RecipientHolder[] recipients, boolean archive, boolean track,
			int delay, String context, EmailAttachment... attachments)
			throws MailException, ValidationException, AccessDeniedException {
		var b = newBuilder(realm);
		b.subject(recipeintSubject);
		b.text(receipientText);
		b.replyToName(replyToName);
		b.replyToName(replyToEmail);
		b.recipients(recipients);
		b.archive(archive);
		b.track(track);
		b.delay(delay);
		b.context(context);
		b.addAttachments(attachments);

		try {
			b.send();
		} catch (IOException ioe) {
			if (ioe.getCause() instanceof MailException) {
				throw (MailException) ioe.getCause();
			} else if (ioe.getCause() instanceof ValidationException) {
				throw (ValidationException) ioe.getCause();
			} else if (ioe.getCause() instanceof AccessDeniedException) {
				throw (AccessDeniedException) ioe.getCause();
			}
			throw new IllegalStateException("Failed to send.", ioe);
		}
	}

	private MessageDeliveryResult send(EmailNotificationBuilder builder) throws MessageDeliveryException {

		if (!isEnabled()) {
			if (systemConfigurationService.getBooleanValue("email.on")) {
				return MessageDeliveryResult.ofNonFatalError("This system is not allowed to send email messages.");
			} else {
				return MessageDeliveryResult.ofNonFatalError(
						"Sending messages is disabled. Enable SMTP settings in System realm to allow sending of emails");
			}
		}

		var results = new MessageDeliveryResult(builder.recipients());

		try (var c = tryWithElevatedPermissions(SystemPermission.SYSTEM)) {

			var mail = mailerService.getMailer(builder.realm());
			var archiveAddress = configurationService.getValue(builder.realm(), "email.archiveAddress");
			var archiveRecipients = new ArrayList<RecipientHolder>();

			if (builder.archive() && StringUtils.isNotBlank(archiveAddress)) {
				archiveRecipients.add(parseRecipient(archiveAddress));
			}

			var noNoReply = configurationService.getBooleanValue(builder.realm(), SMTP_DO_NOT_SEND_TO_NO_REPLY);

			for (var r : builder.recipients()) {

				var result = results.newResult(r);

				if (StringUtils.isBlank(r.getAddress())) {
					var msg = String.format("Missing email address for %s", r.getName());
					log.warn(msg);
					result.skip(msg);
					continue;
				}

				if (noNoReply && isNoReply(StringUtils.left(r.getAddress(), r.getAddress().indexOf('@')))) {
					var msg = String.format("Skipping no reply email address for %s", r.getAddress());
					log.warn(msg);
					result.skip(msg);
					continue;
				}

				var serverResolver = new ServerResolver(builder.realm());

				var messageSubject = processDefaultReplacements(builder.subject(), r, serverResolver);
				var messageText = processDefaultReplacements(builder.text(), r, serverResolver);

				try {
					if (StringUtils.isNotBlank(builder.html())) {

						/**
						 * Send a HTML email and generate tracking if required. Make sure only the
						 * recipients get a tracking email. We don't want to track the archive emails.
						 */
						var trackingImage = configurationService.getValue(builder.realm(), "email.trackingImage");
						var nonTrackingUri = trackerService.generateNonTrackingUri(trackingImage, builder.realm());

						var messageHtml = processDefaultReplacements(builder.html(), r, serverResolver).replace("${htmlTitle}", messageSubject);
						var archiveRecipientHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);

						result.wrap(() -> {
							send(mail, messageSubject, messageText,  getMessageHtml(builder, r, messageSubject, trackingImage, nonTrackingUri,
									messageHtml), builder, r);

							for (var recipient : archiveRecipients) {
								send(mail, messageSubject, messageText, archiveRecipientHtml, builder, recipient);
							}
						});

					} else {
						result.wrap(() -> {
							/**
							 * Send plain email without any tracking
							 */
							send(mail, messageSubject, messageText, "", builder, r);

							for (var recipient : archiveRecipients) {
								send(mail, messageSubject, messageText, "", builder, recipient);
							}
						});


					}
				} catch (Throwable e) {
					log.error("Mail failed", e);
					result.error(e);
				}
			}
		} catch (IOException e) {
			throw new MessageDeliveryException("Failed to send email.", e);
		} catch (ValidationException e) {
			throw new MessageDeliveryException("Failed to validate archive email addresses. Please check configuration.", e);
		}

		return results;
	}

	private String getMessageHtml(EmailNotificationBuilder builder, RecipientHolder r, String messageSubject,
			String trackingImage, String nonTrackingUri, String messageHtml) throws AccessDeniedException {
		if (builder.track() && StringUtils.isNotBlank(trackingImage)) {
			var trackingUri = trackerService.generateTrackingUri(trackingImage, messageSubject,
					r.getName(), r.getAddress(), builder.realm());
			messageHtml = messageHtml.replace("__trackingImage__", trackingUri);
		} else {
			messageHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
		}
		return messageHtml;
	}

	private boolean isNoReply(String addr) {
		for (String a : NO_REPLY_ADDRESSES) {
			if (addr.startsWith(a))
				return true;
		}
		return false;
	}

	private String processDefaultReplacements(String str, RecipientHolder r, ServerResolver serverResolver) {
		str = str.replace("${email}", r.getAddress());
		str = str.replace("${firstName}", r.getFirstName());
		str = str.replace("${fullName}", r.getName());
		str = str.replace("${principalId}", r.getPrincipalId());

		return ReplacementUtils.processTokenReplacements(str, serverResolver);
	}

	@Override
	public final boolean isEnabled() {
		return !"false".equals(System.getProperty("hypersocket.mail", "true"))
				&& systemConfigurationService.getBooleanValue("email.on")
				&& (Objects.isNull(messageDeliveryService.getController()) || messageDeliveryService.getController().canSend(this));
	}

	private void send(Mailer mail, String subject, String plainText, String htmlText, EmailNotificationBuilder builder,
			RecipientHolder r) throws AccessDeniedException {

		Principal p = null;

		if (r.hasPrincipal()) {
			p = r.getPrincipal();
		} else {
			try {
				p = realmService.getPrincipalByEmail(builder.realm(), r.getAddress());
			} catch (ResourceNotFoundException e) {
			}
		}

		if (p != null) {
			if (realmService.getUserPropertyBoolean(p, "user.bannedEmail")) {
				if (log.isInfoEnabled()) {
					log.info("Email to principal {} is banned", r.getAddress());
				}
				return;
			}
		}

		var apiKey = mailerService.getSMTPValue(builder.realm(), "email.sendGridAPIKey");

		if (StringUtils.isNotBlank(apiKey)) {

			var validated = false;
			if (p != null) {
				if (realmService.getUserPropertyBoolean(p, "user.validatedEmail")) {
					validated = true;
				}
			}

			if (!validated) {
				var headers = new HashMap<String, String>();
				headers.put("Authorization", "Bearer " + apiKey);
				headers.put("Content-Type", "application/json");

				var json = "{\"email\": \"" + r.getAddress() + "\", \"source\": \"validate\"}";

				try {
					var response = httpUtils.doHttpPost("https://api.sendgrid.com/v3/validations/email", false, headers,
							json, "application/json", 200);

					var m = new ObjectMapper();

					var node = m.readTree(response);
					var verdict = node.findValue("verdict").asText("Invalid");
					var score = node.findValue("score").asDouble(0D);
					double allowedScore = mailerService.getSMTPIntValue(builder.realm(), "email.sendGridMinScore");
					if (p != null) {
						realmService.setUserPropertyBoolean(p, "user.validatedEmail", true);
					}
					if ("Invalid".equals(verdict) || (allowedScore > 0 && (score * 100) < allowedScore)) {
						if (p != null) {
							realmService.setUserPropertyBoolean(p, "user.bannedEmail", true);
						}
						eventService.publishEvent(new EmailEvent(this,
								new IOException(
										"Email validation failed with score " + score + " and verdict " + verdict),
								builder.realm(), subject, plainText, r.getAddress(), builder.context()));
						return;
					}
				} catch (IOException e) {
					eventService.publishEvent(new EmailEvent(this, e, builder.realm(), subject, plainText,
							r.getAddress(), builder.context()));
					return;
				}
			}

		}

		var fromAddress = mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_FROM_ADDRESS);
		if (r.getAddress().equalsIgnoreCase(fromAddress)) {
			if (log.isInfoEnabled()) {
				log.info("Email loopback detected. The from address {} is the same as the destination", fromAddress,
						r.getAddress());
			}
			return;
		}

		var blocked = Arrays.asList(configurationService.getValues(builder.realm(), "email.blocked"));
		for (var emailAddress : blocked) {
			if (r.getAddress().equalsIgnoreCase(emailAddress)) {
				if (log.isInfoEnabled()) {
					log.info("Email blocked. The destination address {} is blocked", emailAddress);
				}
				return;
			}
		}

		var email = EmailBuilder.startingBlank()
				.from(mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_FROM_NAME), fromAddress);

		if (StringUtils.isNotBlank(builder.replyToName()) && StringUtils.isNotBlank(builder.replyToEmail())) {
			email.withReplyTo(builder.replyToName(), builder.replyToEmail());
		} else if (StringUtils
				.isNotBlank(mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_REPLY_NAME))
				&& StringUtils.isNotBlank(
						mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_REPLY_ADDRESS))) {
			email.withReplyTo(mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_REPLY_NAME),
					mailerService.getSMTPValue(builder.realm(), MailerServiceImpl.SMTP_REPLY_ADDRESS));
		}

		email.to(r.getName(), r.getAddress());

		email.withSubject(subject.equals("") ? "<No Subject>" : subject);

		if (StringUtils.isNotBlank(htmlText)) {
			var doc = Jsoup.parse(htmlText);
			try {
				for (var el : doc.select("img")) {
					var src = el.attr("src");
					if (src != null && src.startsWith("data:")) {
						int idx = src.indexOf(':');
						src = src.substring(idx + 1);
						idx = src.indexOf(';');
						var mime = src.substring(0, idx);
						src = src.substring(idx + 1).trim();
						for (String arg : src.split(";")) {
							arg = arg.trim();
							if (arg.startsWith("name=")) {
								// Ignore
							} else if (arg.startsWith("base64")) {
								idx = arg.indexOf(',');
								var enc = arg.substring(0, idx);
								var data = arg.substring(idx + 1);
								if (!"base64".equals(enc)) {
									throw new UnsupportedOperationException(
											String.format("%s is not supported for embedded images.", enc));
								}
								var bytes = Base64.getDecoder().decode(data);
								var cid = UUID.randomUUID();
								el.attr("src", "cid:" + OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid);
								email.withEmbeddedImage(OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid.toString(),
										bytes, mime);
							} else {
								log.warn(String.format("Unexpected attribute in embedded image data URI. %s", arg));
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(String.format("Failed to parse embedded images in email %s to %s.", subject, r.getAddress()),
						e);
			}
			email.appendTextHTML(doc.toString());
		}

		email.withPlainText(plainText);

		for (EmailAttachment attachment : builder.attachments()) {
			email.withAttachment(attachment.getName(), attachment);
		}

		try {

			if (Boolean.getBoolean("smtp.enableDelay") && builder.delay() > 0) {
				try {
					Thread.sleep(builder.delay());
				} catch (InterruptedException e) {
				}
			}

			if ("true".equals(System.getProperty("hypersocket.email", "true"))) {

//				Boolean sync = synchronousEmail.get();
//				if(Boolean.TRUE.equals(sync)) {
//					AsyncResponse asyncResponse = mail.sendMail(email.buildEmail(), true);
//					asyncResponse.onSuccess(() -> eventService.publishEvent(new EmailEvent(this, realm, subject, plainText, r.getEmail(), context)));
//					asyncResponse.onException((e) -> eventService.publishEvent(new EmailEvent(this, e, realm, subject, plainText, r.getEmail(), context)));
//				} else {
				/**
				 * sendMail actually sends async if you setup the Mailer to be async (the
				 * javadocs are wrong, look at the code).
				 */
				mail.sendMail(email.buildEmail());
				eventService.publishEvent(
						new EmailEvent(this, builder.realm(), subject, plainText, r.getAddress(), builder.context()));
//				}

			}

		} catch (MailException e) {
			eventService.publishEvent(
					new EmailEvent(this, e, builder.realm(), subject, plainText, r.getAddress(), builder.context()));
			throw e;
		}
	}


	private RecipientHolder parseRecipient(String val) throws ValidationException {
		var p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		var m = p.matcher(val);
		Principal principal = null;

		if (m.find()) {
			var name = m.group(1).replaceAll("[\\n\\r]+", "");
			var email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (!Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				throw new ValidationException(email + " is not a valid email address");
			}

			name = WordUtils.capitalize(name.replace('.', ' ').replace('_', ' '));

			principal = realmService.getPrincipalByName(getCurrentRealm(), email, PrincipalType.USER);
		} else {

			// Not an email address? Is this a principal of the realm?
			principal = realmService.getPrincipalByName(getCurrentRealm(), val, PrincipalType.USER);
		}

		if (principal == null) {
			try {
				principal = realmService.getPrincipalByEmail(getCurrentRealm(), val);
			} catch (ResourceNotFoundException e) {
			}
		}

		if (principal == null) {
			try {
				principal = realmService.getPrincipalById(getCurrentRealm(), Long.parseLong(val), PrincipalType.USER);
			} catch (AccessDeniedException | NumberFormatException e) {
			}
		}

		if (principal != null) {
			if (StringUtils.isNotBlank(principal.getEmail())) {
				return new RecipientHolder(principal, principal.getEmail());
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return RecipientHolder.ofAddress(val);
		}
		throw new ValidationException(val + " is not a valid email address");
	}
}
