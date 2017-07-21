package com.hypersocket.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
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
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUploadService;

@Service
public class EmailNotificationServiceImpl extends AbstractAuthenticatedServiceImpl implements EmailNotificationService {

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	RealmService realmService; 
	
	@Autowired
	FileUploadService uploadService; 
	
	@Autowired
	EmailTrackerService trackerService; 
	
	@Autowired
	EventService eventService;
	
	@Autowired
	SystemConfigurationService systemConfigurationService;
	
	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	final static String SMTP_ENABLED = "smtp.enabled";
	final static String SMTP_HOST = "smtp.host";
	final static String SMTP_PORT = "smtp.port";
	final static String SMTP_PROTOCOL = "smtp.protocol";
	final static String SMTP_USERNAME = "smtp.username";
	final static String SMTP_PASSWORD = "smtp.password";
	final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	final static String SMTP_FROM_NAME = "smtp.fromName";
	
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

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
			log.warn("Sending messages is disabled. Enable SMTP settings in System realm to allow sending of emails");
			return;
		}
		
		Mailer mail = new Mailer(getSMTPValue(realm, SMTP_HOST), 
				getSMTPIntValue(realm, SMTP_PORT), 
				getSMTPValue(realm, SMTP_USERNAME),
				getSMTPDecryptedValue(realm, SMTP_PASSWORD),
				TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)]);
		
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
			
			String htmlTemplate = configurationService.getValue(realm, "email.htmlTemplate");
			if(StringUtils.isNotBlank(htmlTemplate) && StringUtils.isNotBlank(receipientHtml)) {
				try {
					htmlTemplate = IOUtils.toString(uploadService.getInputStream(htmlTemplate));
					htmlTemplate = htmlTemplate.replace("${htmlContent}", receipientHtml);
					
					String trackingImage = configurationService.getValue(realm, "email.trackingImage");
					if(track && StringUtils.isNotBlank(trackingImage)) {
						String trackingUri = trackerService.generateTrackingUri(recipeintSubject, r.getName(), r.getEmail(), realm);
						htmlTemplate = htmlTemplate.replace("${trackingImage}", trackingUri);
					} else {
						String trackingUri = trackerService.generateNonTrackingUri(trackingImage, realm);
						htmlTemplate = htmlTemplate.replace("${trackingImage}", trackingUri);
					}

					receipientHtml = htmlTemplate;
				} catch (ResourceNotFoundException e) {
					log.error("Cannot find HTML template", e);
				} catch (IOException e) {
					log.error("Cannot find HTML template", e);
				}	
			}
			
			send(realm, mail, 
					recipeintSubject, 
					receipientText, 
					receipientHtml, 
					replyToName, 
					replyToEmail, 
					track, 
					r, 
					delay,
					attachments);
			
			for(RecipientHolder recipient : archiveRecipients) {
				send(realm, mail, recipeintSubject, receipientText, receipientHtml, 
						replyToName, replyToEmail, false, recipient, delay, attachments);
			}
		}
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
	
	private boolean isEnabled() {
		return systemConfigurationService.getBooleanValue("smtp.on");
	}
	
	private void send(Realm realm, 
			Mailer mail,
			String subject, 
			String plainText, 
			String htmlText, 
			String replyToName, 
			String replyToEmail, 
			boolean track,
			RecipientHolder r, 
			int delay,
			EmailAttachment... attachments) throws AccessDeniedException {
		
		Email email = new Email();
		
		email.setFromAddress(
				getSMTPValue(realm, SMTP_FROM_NAME), 
				getSMTPValue(realm, SMTP_FROM_ADDRESS));
		
		email.addRecipient(r.getName(), r.getEmail(), RecipientType.TO);
		
		email.setSubject(subject);
		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			email.setReplyToAddress(replyToName, replyToEmail);
		}
		
		if(StringUtils.isNotBlank(htmlText)) {
			email.setTextHTML(htmlText);
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
