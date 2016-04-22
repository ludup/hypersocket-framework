package com.hypersocket.inbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.search.FlagTerm;

import org.apache.commons.io.IOUtils;

public class InboxProcessor {

	private Properties generateServerProperties(String protocol, String host, String port) {
		Properties properties = new Properties();

		// server setting
		properties.put(String.format("mail.%s.host", protocol), host);
		properties.put(String.format("mail.%s.port", protocol), port);

		// SSL setting
		properties.setProperty(String.format("mail.%s.socketFactory.class", protocol),
				"javax.net.ssl.SSLSocketFactory");
		properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol), "false");
		properties.setProperty(String.format("mail.%s.socketFactory.port", protocol), String.valueOf(port));

		return properties;
	}

	/**
	 * Downloads new messages and fetches details for each message.
	 * 
	 * @param protocol
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 */
	public void downloadEmails(String protocol, String host, String port, String userName, String password,
			boolean allMessages, EmailProcessor processor) {
		Properties properties = generateServerProperties(protocol, host, port);
		Session session = Session.getDefaultInstance(properties);

		try {
			// connects to the message store
			Store store = session.getStore(protocol);
			store.connect(userName, password);

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);

			// fetches new messages from server
			Message[] messages;
			if (allMessages) {
				messages = folderInbox.getMessages();
			} else {
				messages = folderInbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			}

			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];

				String contentType = msg.getContentType().toLowerCase();
				String textContent = "";
				String htmlContent = "";
				List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
				try {
					if (contentType.contains("text/plain")) {
						textContent = msg.getContent().toString();
					} else if (contentType.contains("text/html")) {
						htmlContent = msg.getContent().toString();
					} else if (contentType.contains("multipart")) {

						Multipart multiPart = (Multipart) msg.getContent();

						for (int x = 0; x < multiPart.getCount(); x++) {
							MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
							contentType = part.getContentType().toLowerCase();
							if (contentType.contains("text/plain")) {
								textContent = part.getContent().toString();
							} else if (contentType.contains("text/html")) {
								htmlContent = part.getContent().toString();
							} else if (Part.INLINE.equalsIgnoreCase(part.getDisposition()) 
									|| Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

								File attachment = File.createTempFile("email", "attachment");
								OutputStream out = new FileOutputStream(attachment);
								InputStream in = part.getInputStream();
								try {
									IOUtils.copy(in, out);
									attachments.add(new EmailAttachment(part.getFileName(), part.getContentType(), attachment));
								} finally {
									IOUtils.closeQuietly(out);
									IOUtils.closeQuietly(in);
								}

							}
						}

					}
					
					//Generate rawMessage file
					// Extract message id
					// Extract IP address received from
					// Strip html content if no plain availabel (JSoup)
					processor.processEmail(msg.getFrom(),
							msg.getReplyTo(),
							msg.getRecipients(RecipientType.TO), 
							msg.getRecipients(RecipientType.CC), 
							msg.getSubject(), 
							textContent, 
							htmlContent, 
							msg.getSentDate(),
							msg.getReceivedDate(),
							attachments.toArray(new EmailAttachment[0]));

				} catch (IOException e) {
					e.printStackTrace();
				}
		
			}

			// disconnect
			folderInbox.close(false);
			store.close();
		} catch (MessagingException ex) {
			System.out.println("Could not connect to the message store");
			ex.printStackTrace();
		}
	}

	/**
	 * Returns a list of addresses in String format separated by comma
	 *
	 * @param address
	 *            an array of Address objects
	 * @return a string represents a list of addresses
	 */
	private String parseAddresses(Address[] address) {
		String listAddress = "";

		if (address != null) {
			for (int i = 0; i < address.length; i++) {
				listAddress += address[i].toString() + ", ";
			}
		}
		if (listAddress.length() > 1) {
			listAddress = listAddress.substring(0, listAddress.length() - 2);
		}

		return listAddress;
	}

	/**
	 * Test downloading e-mail messages
	 */
	public static void main(String[] args) {
		// for POP3
		// String protocol = "pop3";
		// String host = "pop.gmail.com";
		// String port = "995";

		// for IMAP
		String protocol = "imap";
		String host = "imap.gmail.com";
		String port = "993";

		String userName = "lee@ninevehcottages.com";
		String password = "xxxxxxx";

		InboxProcessor receiver = new InboxProcessor();
		receiver.downloadEmails(protocol, host, port, userName, password, true, new EmailProcessor() {
			
			@Override
			public void processEmail(Address[] from, Address[] replyTo, Address[] to, Address[] cc, String subject,
					String textContent, String htmlContent, Date sent, Date received, EmailAttachment... attachments) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
