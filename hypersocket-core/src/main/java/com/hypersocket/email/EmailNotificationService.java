package com.hypersocket.email;

import java.io.File;
import java.util.List;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Recipient;

import com.hypersocket.triggers.ValidationException;

public interface EmailNotificationService {
	
	static final String RESOURCE_BUNDLE = "EmailService";
	
	void sendPlainEmail(String subject, String text, Recipient[] recipients)
			throws MailException;

	void sendHtmlEmail(String subject, String text, Recipient[] recipients)
			throws MailException;

	boolean validateEmailAddress(String email);

	boolean validateEmailAddresses(String[] emails);

	String populateEmailList(String[] emails, List<Recipient> recipients,
			RecipientType type) throws ValidationException;

	void sendPlainEmail(String subject, String text, Recipient[] recipients,
			EmailAttachment[] attachments) throws MailException;

	void sendHtmlEmail(String subject, String text, Recipient[] recipients,
			EmailAttachment[] attachments) throws MailException;

}
