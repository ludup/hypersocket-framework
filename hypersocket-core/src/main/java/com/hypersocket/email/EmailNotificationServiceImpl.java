package com.hypersocket.email;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.Recipient;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
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
	
	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	final static String SMTP_HOST = "smtp.host";
	final static String SMTP_PORT = "smtp.port";
	final static String SMTP_PROTOCOL = "smtp.protocol";
	final static String SMTP_USERNAME = "smtp.username";
	final static String SMTP_PASSWORD = "smtp.password";
	final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	final static String SMTP_FROM_NAME = "smtp.fromName";
	
	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	@Override
	@SafeVarargs
	public final void sendEmail(String subject, String text, String html, Recipient[] recipients, EmailAttachment... attachments) throws MailException {
		sendEmail(getCurrentRealm(), subject, text, html, recipients, attachments);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(Realm realm, String subject, String text, String html, Recipient[] recipients, EmailAttachment... attachments) throws MailException {
		sendEmail(realm, subject, text, html, null, null, recipients, attachments);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(Realm realm, String subject, String text, String html, String replyToName, String replyToEmail, Recipient[] recipients, EmailAttachment... attachments) throws MailException {
		Email email = new Email();
		
		email.setFromAddress(configurationService.getValue(realm, SMTP_FROM_NAME), 
				configurationService.getValue(realm, SMTP_FROM_ADDRESS));
		
		for(Recipient r : recipients) {
			email.addRecipient(r.getName(), r.getAddress(), r.getType());
		}
		
		email.setSubject(subject);
		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			email.setReplyToAddress(replyToName, replyToEmail);
		}
		
		String htmlTemplate = configurationService.getValue(realm, "email.htmlTemplate");
		if(StringUtils.isNotBlank(htmlTemplate) && StringUtils.isNotBlank(html)) {
			try {
				htmlTemplate = FileUtils.readFileToString(uploadService.getFile(htmlTemplate));
				htmlTemplate = htmlTemplate.replace("${htmlContent}", html);
				email.setTextHTML(htmlTemplate);
			} catch (ResourceNotFoundException e) {
				log.error("Cannot find HTML template", e);
			} catch (IOException e) {
				log.error("Cannot find HTML template", e);
			}
			
		} else if(StringUtils.isNotBlank(html)) {
			email.setTextHTML(html);
		}
		
		if(StringUtils.isNotBlank(text)) {
			email.setText(text);
		}
		
		if(attachments!=null) {
			for(EmailAttachment attachment : attachments) {
				email.addAttachment(attachment.getName(), attachment);
			}
		}
		
		Mailer mail = new Mailer(configurationService.getValue(realm, SMTP_HOST), 
				configurationService.getIntValue(realm, SMTP_PORT), 
				configurationService.getValue(realm, SMTP_USERNAME),
				configurationService.getDecryptedValue(realm, SMTP_PASSWORD),
				TransportStrategy.values()[configurationService.getIntValue(realm, SMTP_PROTOCOL)]);
		
		
		mail.sendMail(email);
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
	
	private String[] getEmailName(String val) throws ValidationException {
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return new String[] { name, email };
			} else {
				throw new ValidationException(email
						+ " is not a valid email address");
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			String name = val.substring(0, val.indexOf('@'));
			return new String[] { WordUtils.capitalize(name.replace('.',  ' ').replace('_', ' ')), val };
		}

		// Not an email address? Is this a principal of the realm?
		Principal principal = realmService.getPrincipalByName(getCurrentRealm(), val, PrincipalType.USER);
		
		if(principal!=null) {
			try {
				return new String[] { realmService.getPrincipalDescription(principal),
						realmService.getPrincipalAddress(principal, MediaType.EMAIL)};
			} catch (MediaNotFoundException e) {
				log.error("Could not find email address for " + val, e);
			}
		}
		throw new ValidationException(val
				+ " is not a valid email address");
	}
	
	@Override
	public String populateEmailList(String[] emails, 
			List<Recipient> recipients,
			RecipientType type)
			throws ValidationException {

		StringBuffer ret = new StringBuffer();

		for (String email : emails) {

			if (ret.length() > 0) {
				ret.append(", ");
			}
			ret.append(email);
			String[] rec = getEmailName(email);
			recipients.add(new Recipient(rec[0], rec[1], type));
		}

		return ret.toString();
	}
}
