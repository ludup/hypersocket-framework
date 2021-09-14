package com.hypersocket.email;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.net.ssl.SSLSocketFactory;
import javax.mail.Session;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.http.auth.InvalidCredentialsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.ServerResolver;
import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.triggers.ValidationException;

@Service
public class EmailNotificationServiceImpl extends AbstractAuthenticatedServiceImpl implements EmailNotificationService {
	
	private final static List<String> NO_REPLY_ADDRESSES = Arrays.asList("noreply", "no.reply", "no-reply", "no_reply", "do_not_reply", "do.not.reply", "do_not_reply");

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
	
	private EmailController controller; 
	
	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	public final static String SMTP_ENABLED = "smtp.enabled";
	public final static String SMTP_HOST = "smtp.host";
	public final static String SMTP_PORT = "smtp.port";
	public final static String SMTP_PROTOCOL = "smtp.protocol";
	public final static String SMTP_USERNAME = "smtp.username";
	public final static String SMTP_SESSION_TIMEOUT = "smtp.sessionTimeout";
	public final static String SMTP_PASSWORD = "smtp.password";
	public final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	public final static String SMTP_FROM_NAME = "smtp.fromName";
	public final static String SMTP_REPLY_ADDRESS = "smtp.replyAddress";
	public final static String SMTP_REPLY_NAME = "smtp.replyName";
	public final static String SMTP_DO_NOT_SEND_TO_NO_REPLY = "smtp.doNotSendToNoReply";
	
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	public static final String OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX = "OGIAU";

	@Override
	@SafeVarargs
	public final void sendEmail(String subject, String text, String html, RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(getCurrentRealm(), subject, text, html, recipients, archive, track, delay, context, attachments);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(Realm realm, String subject, String text, String html, RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(realm, subject, text, html, null, null, recipients, archive, track, delay, context, attachments);
	}
	
	public EmailController getController() {
		return controller;
	}

	public void setController(EmailController controller) {
		this.controller = controller;
	}

	private Session createSession(Realm realm) {
		
		Properties properties = new Properties();
	    properties.put("mail.smtp.auth", "false");
	    properties.put("mail.smtp.starttls.enable", TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)]==TransportStrategy.SMTP ? "false" : "true");
	    properties.put("mail.smtp.host", getSMTPValue(realm, SMTP_HOST));
	    properties.put("mail.smtp.port", getSMTPIntValue(realm, SMTP_PORT));
	    properties.put("mail.smtp.ssl.checkserveridentity", "false");
	    properties.put("mail.smtp.ssl.trust", "*");
	    return Session.getInstance(properties);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(Realm realm, 
			String recipeintSubject, 
			String receipientText, 
			String receipientHtml, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder[] recipients, 
			boolean archive,
			boolean track,
			int delay,
			String context, EmailAttachment... attachments) throws MailException, ValidationException, AccessDeniedException {
		
		if(!isEnabled()) {
			if(systemConfigurationService.getBooleanValue("smtp.on")) {
				log.warn("This system is not allowed to send email messages.");
			} else {
				log.warn("Sending messages is disabled. Enable SMTP settings in System realm to allow sending of emails");
			}
			return;
		}
		
		elevatePermissions(SystemPermission.SYSTEM);
		
		try {
		
			Mailer mail;
					
			
			if(StringUtils.isNotBlank(getSMTPValue(realm, SMTP_USERNAME))) {
				mail = MailerBuilder.withSMTPServer(getSMTPValue(realm, SMTP_HOST), getSMTPIntValue(realm, SMTP_PORT))
					.withSMTPServerUsername(getSMTPValue(realm, SMTP_USERNAME))
					.withSMTPServerPassword(getSMTPDecryptedValue(realm, SMTP_PASSWORD))
					.withTransportStrategy(TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)])
					.withConnectionPoolCoreSize(1)
				    .withProperty("mail.smtp.ssl.checkserveridentity", false)
				    .withProperty("mail.smtp.ssl.trust", "*")
					.withSessionTimeout(getSMTPIntValue(realm, SMTP_SESSION_TIMEOUT) * 1000)
					.buildMailer();
			} else {
				mail = MailerBuilder.withSMTPServer(getSMTPValue(realm, SMTP_HOST), getSMTPIntValue(realm, SMTP_PORT))
						.withTransportStrategy(TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)])
						.withConnectionPoolCoreSize(1)
					    .withProperty("mail.smtp.ssl.checkserveridentity", false)
					    .withProperty("mail.smtp.ssl.trust", "*")
						.withSessionTimeout(getSMTPIntValue(realm, SMTP_SESSION_TIMEOUT) * 1000)
						.buildMailer();
			}
			
//			mail.setSessionTimeout(getSMTPIntValue(realm, SMTP_SESSION_TIMEOUT) * 1000);
			
			String archiveAddress = configurationService.getValue(realm, "email.archiveAddress");
			List<RecipientHolder> archiveRecipients = new ArrayList<RecipientHolder>();
	
			if(archive && StringUtils.isNotBlank(archiveAddress)) {
				populateEmailList(new String[] {archiveAddress} , archiveRecipients, RecipientType.TO);
			}
			
			boolean noNoReply = configurationService.getBooleanValue(realm, SMTP_DO_NOT_SEND_TO_NO_REPLY);
	
			for(RecipientHolder r : recipients) {
				
				if(StringUtils.isBlank(r.getEmail())) {
					log.warn(String.format("Missing email address for %s", r.getName()));
					continue;
				}
				
				if(noNoReply && isNoReply(StringUtils.left(r.getEmail(), r.getEmail().indexOf('@')))) {
					log.warn(String.format("Skipping no reply email address for %s", r.getEmail()));
					continue;
				}
				
				
				ServerResolver serverResolver = new ServerResolver(realm);
				
				String messageSubject = processDefaultReplacements(recipeintSubject, r, serverResolver);
				String messageText = processDefaultReplacements(receipientText, r, serverResolver);
				
				
				if(StringUtils.isNotBlank(receipientHtml)) {
				
					/**
					 * Send a HTML email and generate tracking if required. Make sure
					 * only the recipients get a tracking email. We don't want to 
					 * track the archive emails.
					 */
					String trackingImage = configurationService.getValue(realm, "email.trackingImage");
					String nonTrackingUri = trackerService.generateNonTrackingUri(trackingImage, realm);
					
					String messageHtml = processDefaultReplacements(receipientHtml, r, serverResolver);
					messageHtml = messageHtml.replace("${htmlTitle}", messageSubject);
					
					String archiveRecipientHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
	
					if(track && StringUtils.isNotBlank(trackingImage)) {
						String trackingUri = trackerService.generateTrackingUri(trackingImage, messageSubject, r.getName(), r.getEmail(), realm);
						messageHtml = messageHtml.replace("__trackingImage__", trackingUri);
					} else {
						messageHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
					}
	
					send(realm, mail, 
							messageSubject, 
							messageText,
							messageHtml, 
							replyToName, 
							replyToEmail, 
							r, 
							delay,
							context, attachments);
					
					for(RecipientHolder recipient : archiveRecipients) {
						send(realm, mail, messageSubject, messageText, archiveRecipientHtml, 
								replyToName, replyToEmail, recipient, delay, context, attachments);
					}
					
				} else {
				
					/**
					 * Send plain email without any tracking
					 */
					send(realm, mail, 
							messageSubject, 
							messageText, 
							"", 
							replyToName, 
							replyToEmail, 
							r, 
							delay,
							context, attachments);
					
					for(RecipientHolder recipient : archiveRecipients) {
						send(realm, mail, messageSubject, messageText, "", 
								replyToName, replyToEmail, recipient, delay, context, attachments);
					}
				}
			}
		} catch(Throwable e) { 
			log.error("Mail failed", e);
			throw e;
		}finally {
			clearElevatedPermissions();
		}
	}
	
	private boolean isNoReply(String addr) {
		for(String a : NO_REPLY_ADDRESSES) {
			if(addr.startsWith(a))
				return true;
		}
		return false;
	}
	
	private String processDefaultReplacements(String str, RecipientHolder r, ServerResolver serverResolver) {
		str = str.replace("${email}", r.getEmail());
		str = str.replace("${firstName}", r.getFirstName());
		str = str.replace("${fullName}", r.getName());
		str = str.replace("${principalId}", r.getPrincipalId());
		
		return ReplacementUtils.processTokenReplacements(str, serverResolver);
	}

	private String getSMTPValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getValue(realm, name);
	}
	
	private int getSMTPIntValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getIntValue(realm, name);
	}
	
	private String getSMTPDecryptedValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getDecryptedValue(realm, name);
	}
	
	public final boolean isEnabled() {
		return !"false".equals(System.getProperty("hypersocket.mail", "true")) && systemConfigurationService.getBooleanValue("smtp.on") && (Objects.isNull(controller) || controller.canSend());
	}
	
	private void send(Realm realm, 
			Mailer mail,
			String subject, 
			String plainText, 
			String htmlText, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder r, 
			int delay,
			String context, EmailAttachment... attachments) throws AccessDeniedException {
		
		Principal p = null;
		
		if(r.hasPrincipal()) {
			p = r.getPrincipal();
		} else {
			try {
				p = realmService.getPrincipalByEmail(realm, r.getEmail());
			} catch (AccessDeniedException | ResourceNotFoundException e) {
			}
		}
		
		if(p!=null) {
			if(realmService.getUserPropertyBoolean(p, "user.bannedEmail")) {
				if(log.isInfoEnabled()) {
					log.info("Email to principal {} is banned", r.getEmail());
				}
				return;
			}
		}
		
		String fromAddress = getSMTPValue(realm, SMTP_FROM_ADDRESS);
		if(r.getEmail().equalsIgnoreCase(fromAddress)) {
			if(log.isInfoEnabled()) {
				log.info("Email loopback detected. The from address {} is the same as the destination", fromAddress, r.getEmail());
			}
			return;
		}
		
		List<String> blocked = Arrays.asList(configurationService.getValues(realm, "email.blocked"));
		for(String emailAddress : blocked) {
			if(r.getEmail().equalsIgnoreCase(emailAddress)) {
				if(log.isInfoEnabled()) {
					log.info("Email blocked. The destination address {} is blocked", emailAddress);
				}
				return;
			}
		}
		
		EmailPopulatingBuilder email = EmailBuilder.startingBlank().from(getSMTPValue(realm, SMTP_FROM_NAME), fromAddress);
		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			email.withReplyTo(replyToName, replyToEmail);
		} else if(StringUtils.isNotBlank(getSMTPValue(realm, SMTP_REPLY_NAME))
				&& StringUtils.isNotBlank(getSMTPValue(realm, SMTP_REPLY_ADDRESS))) {
			email.withReplyTo(getSMTPValue(realm, SMTP_REPLY_NAME), getSMTPValue(realm, SMTP_REPLY_ADDRESS));
		}
		
		email.to(r.getName(), r.getEmail());
		
		email.withSubject(subject.equals("") ? "<No Subject>" : subject);

		if(StringUtils.isNotBlank(htmlText)) {
			Document doc = Jsoup.parse(htmlText);
			try {
				for (Element el : doc.select("img")) {
					String src = el.attr("src");
					if(src != null && src.startsWith("data:")) {
						int idx = src.indexOf(':');
						src = src.substring(idx + 1);
						idx = src.indexOf(';');
						String mime = src.substring(0, idx);
						src = src.substring(idx + 1).trim();
						for(String arg : src.split(";")) {
							arg = arg.trim();
							if(arg.startsWith("name=")) {
								// Ignore
							}
							else if(arg.startsWith("base64")) {
								idx = arg.indexOf(',');
								String enc = arg.substring(0, idx);
								String data = arg.substring(idx + 1);
								if(!"base64".equals(enc)) {
									throw new UnsupportedOperationException(String.format("%s is not supported for embedded images.", enc));
								}
								byte[] bytes = Base64.decodeBase64(data);
								UUID cid = UUID.randomUUID();
								el.attr("src", "cid:" + OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid);
								email.withEmbeddedImage(OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid.toString(), bytes, mime);
							}
							else {
								log.warn(String.format("Unexpected attribute in embedded image data URI. %s", arg));
							}
						}
					}
				}
			}
			catch(Exception e) {
				log.error(String.format("Failed to parse embedded images in email %s to %s.", subject, r.getEmail()), e);
			}
			email.appendTextHTML(doc.toString());
		}
		
		email.withPlainText(plainText);
		
		if(attachments!=null) {
			for(EmailAttachment attachment : attachments) {
				email.withAttachment(attachment.getName(), attachment);
			}
		}
		
		try {
			
			if(delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				};
			}
			
			if("true".equals(System.getProperty("hypersocket.email", "true")))
				mail.sendMail(email.buildEmail());
			
			eventService.publishEvent(new EmailEvent(this, realm, subject, plainText, r.getEmail(), context));
		} catch (MailException e) {
			eventService.publishEvent(new EmailEvent(this, e, realm, subject, plainText, r.getEmail(), context));
			throw e;
		}
	}
	
	@Override
	public boolean validateEmailAddresses(String[] emails) {
		for(String email : emails) {
			if(!validateEmailAddress(email)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateEmailAddress(String val) {
		
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return true;
			} else {
				return false;
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return true;
		}

		// Not an email address? Is this a principal of the realm?
		Principal principal = realmService.getPrincipalByName(getCurrentRealm(), val, PrincipalType.USER);
		
		if(principal!=null) {
			return StringUtils.isNotBlank(principal.getEmail());
		}
		return false;
	}
	
	private void populateEmail(String val, List<RecipientHolder> recipients) throws ValidationException {

		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);
		Principal principal = null;
		
		if (m.find()) {
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (!Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				throw new ValidationException(email
						+ " is not a valid email address");
			}
			
			name = WordUtils.capitalize(name.replace('.',  ' ').replace('_', ' '));
			
			principal = realmService.getPrincipalByName(getCurrentRealm(), email, PrincipalType.USER);
		} else {

			// Not an email address? Is this a principal of the realm?
			principal = realmService.getPrincipalByName(getCurrentRealm(), val, PrincipalType.USER);
		}
		
		if(principal==null) {
			try {
				principal = realmService.getPrincipalByEmail(getCurrentRealm(), val);
			} catch (ResourceNotFoundException | AccessDeniedException e) {
			}
		}
		
		if(principal==null) {
			try {
				principal = realmService.getPrincipalById(getCurrentRealm(), Long.parseLong(val), PrincipalType.USER);
			} catch(AccessDeniedException | NumberFormatException e) { };
		}
		
		if(principal!=null) {
			if(StringUtils.isNotBlank(principal.getEmail())) {
				recipients.add(new RecipientHolder(principal, principal.getEmail()));
				return;
			}
		}
		
		if(Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			recipients.add(new RecipientHolder("", val));
			return;
		}
		throw new ValidationException(val
				+ " is not a valid email address");
	}
	
	@Override
	public String populateEmailList(String[] emails, 
			List<RecipientHolder> recipients,
			RecipientType type)
			throws ValidationException {

		StringBuffer ret = new StringBuffer();

		for (String email : emails) {

			if (ret.length() > 0) {
				ret.append(", ");
			}
			ret.append(email);
			populateEmail(email, recipients);
		}

		return ret.toString();
	}
	
	@Override
	public String getEmailName(String val) throws InvalidCredentialsException {
		
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			return m.group(1).replaceAll("[\\n\\r]+", "");
		}
		
		return "";
	}
	
	@Override
	public String getEmailAddress(String val) throws InvalidCredentialsException {
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return email;
			} else {
				throw new InvalidCredentialsException();
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return val;
		}
		
		throw new InvalidCredentialsException();
	}
}
