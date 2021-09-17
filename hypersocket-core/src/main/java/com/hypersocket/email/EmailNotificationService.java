package com.hypersocket.email;

import java.util.List;

import javax.mail.Message.RecipientType;

import org.apache.http.auth.InvalidCredentialsException;
import org.simplejavamail.MailException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public interface EmailNotificationService {
	
	static final String RESOURCE_BUNDLE = "EmailService";
	
	boolean validateEmailAddress(String email);

	boolean validateEmailAddresses(String[] emails);

	String populateEmailList(String[] emails, List<RecipientHolder> recipients,
			RecipientType type) throws ValidationException;

	void sendEmail(String subject, String text, String html, RecipientHolder[] recipients,
			boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException;

	void sendEmail(Realm realm, String subject, String text, String html, RecipientHolder[] recipients, boolean archive, boolean track, 
			int delay, String context, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException;

	void sendEmail(Realm realm, String subject, String text, String html, String replyToName, String replyToEmail,
			RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, ValidationException, AccessDeniedException;

	void setController(EmailController controller);

	boolean isEnabled();

	String getEmailName(String val) throws InvalidCredentialsException;

	String getEmailAddress(String val) throws InvalidCredentialsException;

	void enableSynchronousEmail();

	void disableSynchronousEmail();

}
