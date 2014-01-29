package com.hypersocket.email;

import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Recipient;

public interface EmailNotificationService {
	
	static final String RESOURCE_BUNDLE = "EmailService";
	
	void sendPlainEmail(String subject, String text, Recipient[] recipients)
			throws MailException;

	void sendHtmlEmail(String subject, String text, Recipient[] recipients)
			throws MailException;

}
