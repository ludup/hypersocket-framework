package com.hypersocket.email;

import org.simplejavamail.MailException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public interface EmailNotificationService extends EmailMessageDeliveryProvider<EmailNotificationBuilder> {
	
	static final String RESOURCE_BUNDLE = "EmailService";

	@Deprecated
	void sendEmail(String subject, String text, String html, RecipientHolder[] recipients,
			boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException;

	@Deprecated
	void sendEmail(Realm realm, String subject, String text, String html, RecipientHolder[] recipients, boolean archive, boolean track, 
			int delay, String context, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException;

	@Deprecated
	void sendEmail(Realm realm, String subject, String text, String html, String replyToName, String replyToEmail,
			RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, ValidationException, AccessDeniedException;


}
