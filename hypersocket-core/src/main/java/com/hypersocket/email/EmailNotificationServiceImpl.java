package com.hypersocket.email;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.Recipient;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.session.SessionServiceImpl;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

	@Autowired
	ConfigurationService configurationService;

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	final static String SMTP_HOST = "smtp.host";
	final static String SMTP_PORT = "smtp.port";
	final static String SMTP_PROTOCOL = "smtp.protocol";
	final static String SMTP_USERNAME = "smtp.username";
	final static String SMTP_PASSWORD = "smtp.password";
	final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	final static String SMTP_FROM_NAME = "smtp.fromName";
	

	@Override
	public void sendPlainEmail(String subject, String text, Recipient[] recipients) throws MailException {
		sendEmail(subject, text, recipients, false);
	}
	
	@Override
	public void sendHtmlEmail(String subject, String text, Recipient[] recipients) throws MailException {
		sendEmail(subject, text, recipients, true);
	}
	
	private void sendEmail(String subject, String text, Recipient[] recipients, boolean html) throws MailException {
		Email email = new Email();
		
		email.setFromAddress(configurationService.getValue(SMTP_FROM_NAME), 
				configurationService.getValue(SMTP_FROM_ADDRESS));
		
		for(Recipient r : recipients) {
			email.addRecipient(r.getName(), r.getAddress(), r.getType());
		}
		
		email.setSubject(subject);
		
		if(html) {
			email.setTextHTML(text);
		} else {
			email.setText(text);
		}

		Mailer mail = new Mailer(configurationService.getValue(SMTP_HOST), 
				configurationService.getIntValue(SMTP_PORT), 
				configurationService.getValue(SMTP_USERNAME),
				configurationService.getValue(SMTP_PASSWORD),
				TransportStrategy.values()[configurationService.getIntValue(SMTP_PROTOCOL)]);
		
		
		mail.sendMail(email);
	}
}
