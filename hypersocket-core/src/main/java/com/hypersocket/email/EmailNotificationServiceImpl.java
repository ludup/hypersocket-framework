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
import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
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
	
	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

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
			String subject, 
			String text, 
			String html, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder[] recipients, 
			String[] archiveAddresses,
			boolean track,
			int delay,
			EmailAttachment... attachments) throws MailException, ValidationException, AccessDeniedException {
		
		Mailer mail = new Mailer(configurationService.getValue(realm, SMTP_HOST), 
				configurationService.getIntValue(realm, SMTP_PORT), 
				configurationService.getValue(realm, SMTP_USERNAME),
				configurationService.getDecryptedValue(realm, SMTP_PASSWORD),
				TransportStrategy.values()[configurationService.getIntValue(realm, SMTP_PROTOCOL)]);
		
		String archiveAddress = configurationService.getValue(realm, "email.archiveAddress");
		List<RecipientHolder> archiveRecipients = new ArrayList<RecipientHolder>();

		if(StringUtils.isNotBlank(archiveAddress)) {
			populateEmailList(new String[] {archiveAddress} , archiveRecipients, RecipientType.TO);
		}
		
		populateEmailList(archiveAddresses, archiveRecipients, RecipientType.TO);

		for(RecipientHolder r : recipients) {
			
			String recipeintSubject = replaceServerInfo(realm, replaceRecipientInfo(subject, r));
			String receipientText = replaceServerInfo(realm, replaceRecipientInfo(text, r));
			String receipientHtml = replaceServerInfo(realm, replaceRecipientInfo(html, r));
			
			String htmlTemplate = configurationService.getValue(realm, "email.htmlTemplate");
			if(StringUtils.isNotBlank(htmlTemplate) && StringUtils.isNotBlank(receipientHtml)) {
				try {
					htmlTemplate = IOUtils.toString(uploadService.getInputStream(htmlTemplate));
					htmlTemplate = replaceRecipientInfo(htmlTemplate.replace("${htmlContent}", receipientHtml), r);
					
					String trackingImage = configurationService.getValue(realm, "email.trackingImage");
					if(track && StringUtils.isNotBlank(trackingImage)) {
						String trackingUri = trackerService.generateTrackingUri(subject, r.getName(), r.getEmail(), realm);
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
	
	private String replaceRecipientInfo(String str, RecipientHolder r) {
		if(str!=null) {
			return str.replace("${email}", r.getEmail())
					.replace("${firstName}", r.getFirstName())
					.replace("${fullName}", r.getName())
					.replace("${principalId}", r.getPrincipalId());
		} else {
			return str;
		}
	}
	
	private String replaceServerInfo(Realm realm, String str) {
		String serverUrl = configurationService.getValue(realm,"email.externalHostname");
		if(StringUtils.isBlank(serverUrl)) {
			serverUrl = realmService.getRealmHostname(realm);
		}
		if(!serverUrl.startsWith("http")) {
			serverUrl = String.format("https://%s/", serverUrl);
		}
		
		return str.replace("${serverName}", configurationService.getValue(realm, "email.serverName"))
				.replace("${serverUrl}", serverUrl);
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
		
		email.setFromAddress(configurationService.getValue(realm, SMTP_FROM_NAME), 
				configurationService.getValue(realm, SMTP_FROM_ADDRESS));
		
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
			try {
				realmService.getPrincipalAddress(principal, MediaType.EMAIL);
				return true;
			} catch (MediaNotFoundException e) {
			}
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
			try {
				recipients.add(new RecipientHolder(principal,
					realmService.getPrincipalAddress(principal, MediaType.EMAIL)));
				return;
			} catch (MediaNotFoundException e) {
				log.error("Could not find email address for " + val, e);
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
