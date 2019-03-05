package com.hypersocket.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.Session;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
	public final static String SMTP_PASSWORD = "smtp.password";
	public final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	public final static String SMTP_FROM_NAME = "smtp.fromName";
	public final static String SMTP_REPLY_ADDRESS = "smtp.replyAddress";
	public final static String SMTP_REPLY_NAME = "smtp.replyName";
	
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	public static final String OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX = "OGIAU";

	@Override
	@SafeVarargs
	public final void sendEmail(String subject, String text, String html, RecipientHolder[] recipients, boolean track, int delay, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(getCurrentRealm(), subject, text, html, recipients, track, delay, attachments);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(Realm realm, String subject, String text, String html, RecipientHolder[] recipients, boolean track, int delay, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(realm, subject, text, html, null, null, recipients, track, delay, attachments);
	}
	
	@Override
	public void sendEmail(Realm realm, String subject, String text, String html, String replyToName,
			String replyToEmail, RecipientHolder[] recipients, boolean track, int delay, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException {
		sendEmail(realm, subject, text, html, replyToName, replyToEmail, recipients, new String[0], track, delay, attachments);
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
	    properties.put("mail.smtp.starttls.enable", TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)]==TransportStrategy.SMTP_PLAIN ? "false" : "true");
	    properties.put("mail.smtp.host", getSMTPValue(realm, SMTP_HOST));
	    properties.put("mail.smtp.port", getSMTPIntValue(realm, SMTP_PORT));

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
			String[] archiveAddresses,
			boolean track,
			int delay,
			EmailAttachment... attachments) throws MailException, ValidationException, AccessDeniedException {
		
		if(!isEnabled()) {
			if(systemConfigurationService.getBooleanValue("smtp.on")) {
				log.warn("This system is not allowed to send email messages.");
			} else {
				log.warn("Sending messages is disabled. Enable SMTP settings in System realm to allow sending of emails");
			}
			return;
		}
		
		Mailer mail;
		
		if(StringUtils.isNotBlank(getSMTPValue(realm, SMTP_USERNAME))) {
			mail = new Mailer(getSMTPValue(realm, SMTP_HOST), 
					getSMTPIntValue(realm, SMTP_PORT), 
					getSMTPValue(realm, SMTP_USERNAME),
					getSMTPDecryptedValue(realm, SMTP_PASSWORD),
					TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)]);
		} else {
			mail = new Mailer(createSession(realm));
		}
		
		String archiveAddress = configurationService.getValue(realm, "email.archiveAddress");
		List<RecipientHolder> archiveRecipients = new ArrayList<RecipientHolder>();

		if(StringUtils.isNotBlank(archiveAddress)) {
			populateEmailList(new String[] {archiveAddress} , archiveRecipients, RecipientType.TO);
		}

		for(RecipientHolder r : recipients) {
			
			if(StringUtils.isBlank(r.getEmail())) {
				log.warn(String.format("Missing email address for %s", r.getName()));
				continue;
			}
			
			ServerResolver serverResolver = new ServerResolver(realm);
			
			recipeintSubject = processDefaultReplacements(recipeintSubject, r, serverResolver);
			receipientText = processDefaultReplacements(receipientText, r, serverResolver);
			
			if(StringUtils.isNotBlank(receipientHtml)) {
			
				/**
				 * Send a HTML email and generate tracking if required. Make sure
				 * only the recipients get a tracking email. We don't want to 
				 * track the archive emails.
				 */
				String trackingImage = configurationService.getValue(realm, "email.trackingImage");
				String nonTrackingUri = trackerService.generateNonTrackingUri(trackingImage, realm);
				String nonTrackingHtml = receipientHtml.replace("${trackingImage}", nonTrackingUri);
				nonTrackingHtml = nonTrackingHtml.replace("${htmlTitle}", recipeintSubject);
				

				if(track && StringUtils.isNotBlank(trackingImage)) {
					String trackingUri = trackerService.generateTrackingUri(trackingImage, recipeintSubject, r.getName(), r.getEmail(), realm);
					receipientHtml = receipientHtml.replace("${trackingImage}", trackingUri);
				} else {
					receipientHtml = receipientHtml.replace("${trackingImage}", nonTrackingUri);
				}
				
				
				receipientHtml = receipientHtml.replace("${htmlTitle}", recipeintSubject);
				
				send(realm, mail, 
						recipeintSubject, 
						receipientText, 
						"", 
						replyToName, 
						replyToEmail, 
						r, 
						delay,
						attachments);
				
				
				for(RecipientHolder recipient : archiveRecipients) {
					send(realm, mail, recipeintSubject, receipientText, nonTrackingHtml, 
							replyToName, replyToEmail, recipient, delay, attachments);
				}
				
			} else {
			
				/**
				 * Send plain email without any tracking
				 */
				send(realm, mail, 
						recipeintSubject, 
						receipientText, 
						"", 
						replyToName, 
						replyToEmail, 
						r, 
						delay,
						attachments);
				
				for(RecipientHolder recipient : archiveRecipients) {
					send(realm, mail, recipeintSubject, receipientText, "", 
							replyToName, replyToEmail, recipient, delay, attachments);
				}
			}
			
			receipientHtml = processDefaultReplacements(receipientHtml, r, serverResolver);
			
			
			
			
		}
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
		return systemConfigurationService.getBooleanValue("smtp.on") && (Objects.isNull(controller) || controller.canSend());
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
			EmailAttachment... attachments) throws AccessDeniedException {
		
		Email email = new Email();
		
		email.setFromAddress(
				getSMTPValue(realm, SMTP_FROM_NAME), 
				getSMTPValue(realm, SMTP_FROM_ADDRESS));
		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			email.setReplyToAddress(replyToName, replyToEmail);
		} else if(StringUtils.isNotBlank(getSMTPValue(realm, SMTP_REPLY_NAME))
				&& StringUtils.isNotBlank(getSMTPValue(realm, SMTP_REPLY_ADDRESS))) {
			email.setReplyToAddress(getSMTPValue(realm, SMTP_REPLY_NAME), getSMTPValue(realm, SMTP_REPLY_ADDRESS));
		}
		
		email.addRecipient(r.getName(), r.getEmail(), RecipientType.TO);
		
		email.setSubject(subject);

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
						src = src.substring(idx + 1);
						idx = src.indexOf(',');
						String enc = src.substring(0, idx);
						String data = src.substring(idx + 1);
						if(!"base64".equals(enc)) {
							throw new UnsupportedOperationException(String.format("%s is not supported for embedded images.", enc));
						}
						byte[] bytes = Base64.decodeBase64(data);
						UUID cid = UUID.randomUUID();
						el.attr("src", "cid:" + OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid);
						email.addEmbeddedImage(OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid.toString(), bytes, mime);
					}
				}
			}
			catch(Exception e) {
				log.error(String.format("Failed to parse embedded images in email %s to %s.", subject, r.getEmail()), e);
			}
			email.setTextHTML(doc.toString());
		}
		
		email.setText(plainText);
		
		if(attachments!=null) {
			for(EmailAttachment attachment : attachments) {
				email.addAttachment(attachment.getName(), attachment);
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
				mail.sendMail(email);
			
			eventService.publishEvent(new EmailEvent(this, realm, subject, plainText, r.getEmail()));
		} catch (MailException e) {
			eventService.publishEvent(new EmailEvent(this, e, realm, subject, plainText, r.getEmail()));
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
}
