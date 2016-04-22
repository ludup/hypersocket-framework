package com.hypersocket.inbox;

import java.util.Date;

import javax.mail.Address;

public interface EmailProcessor {

	void processEmail(Address[] from, Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent,
			String htmlContent, Date sent, Date received, EmailAttachment... attachments);
}
