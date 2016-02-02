package com.hypersocket.email;

import java.util.List;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Recipient;

import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public interface EmailNotificationService {
	
	static final String RESOURCE_BUNDLE = "EmailService";
	
	boolean validateEmailAddress(String email);

	boolean validateEmailAddresses(String[] emails);

	String populateEmailList(String[] emails, List<Recipient> recipients,
			RecipientType type) throws ValidationException;

	void sendEmail(String subject, String text, String html, Recipient[] recipients, EmailAttachment... attachments)
			throws MailException;

	void sendEmail(Realm realm, String subject, String text, String html, Recipient[] recipients,
			EmailAttachment... attachments) throws MailException;

	void sendEmail(Realm realm, String subject, String text, String html, String replyToName, String replyToEmail,
			Recipient[] recipients, EmailAttachment... attachments) throws MailException;

}
