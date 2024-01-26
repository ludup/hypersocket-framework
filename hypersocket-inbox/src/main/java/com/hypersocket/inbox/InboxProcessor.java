package com.hypersocket.inbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.search.FlagTerm;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboxProcessor {

	static Logger log = LoggerFactory.getLogger(InboxProcessor.class);
	
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
	 * @throws MessagingException 
	 */
	public void downloadEmails(String protocol, String host, String port, String userName, String password,
			boolean allMessages, boolean secure, EmailProcessor processor) throws MessagingException {

		if (protocol.equals("imap") && secure) {
			protocol = "imaps";
		}

		Properties properties = generateServerProperties(protocol, host, port);
		Session session = Session.getInstance(properties);

		// connects to the message store
		Store store = session.getStore(protocol);
		try {
			store.connect(userName, password);

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			if (allMessages)
				folderInbox.open(Folder.READ_ONLY);
			else
				folderInbox.open(Folder.READ_WRITE);
			try {

				// fetches new messages from server
				Message[] messages;
				if (allMessages) {
					messages = folderInbox.getMessages();
				} else {
					messages = folderInbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
				}

				for (int i = 0; i < messages.length; i++) {
					Message msg = messages[i];

					if (!allMessages)
						msg.setFlag(Flag.SEEN, true);

					parseMessage(msg, processor);

				}
			} finally {
				folderInbox.close(false);
			}
		} finally {
			// disconnect
			store.close();
		}
	}

	public void parseMessage(Message msg, EmailProcessor processor) throws MessagingException {
		
		
		String contentType = msg.getContentType().toLowerCase();
		StringBuffer textContent = new StringBuffer();
		StringBuffer htmlContent = new StringBuffer();
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		try {
			
			StringBuffer rawContent = new StringBuffer();
			
			Enumeration<Header> headers = msg.getAllHeaders();
			while(headers.hasMoreElements()) {
				Header header = headers.nextElement();
				rawContent.append(header.getName());
				rawContent.append(": ");
				rawContent.append(header.getValue());
				rawContent.append(System.lineSeparator());
			}
			
			rawContent.append(IOUtils.toString(msg.getInputStream(), "UTF-8"));
			
			if (contentType.contains("multipart") || msg.getContent() instanceof Multipart) {
				processMultipart((Multipart) msg.getContent(), textContent, htmlContent, attachments);
			} else if (contentType.contains("text/plain")) {
				textContent.append(msg.getContent().toString());
			} else if (contentType.contains("text/html")) {
				htmlContent.append(msg.getContent().toString());
			} else {
				try {
				File attachment = File.createTempFile("email", "attachment");
				OutputStream out = new FileOutputStream(attachment);
				InputStream in = msg.getInputStream();
				try {
					IOUtils.copy(in, out);
					attachments.add(new EmailAttachment(msg.getFileName(), msg.getContentType(), attachment));
				} finally {
					IOUtils.closeQuietly(out);
					IOUtils.closeQuietly(in);
				}
				} catch(Throwable t) {
					log.error("Tried to parse email attachment of content type {} but failed", msg.getContentType(), t);
				}
			} 

			processor.processEmail(msg.getFrom(), msg.getReplyTo(), msg.getRecipients(RecipientType.TO),
					msg.getRecipients(RecipientType.CC), msg.getSubject(), textContent.toString(),
					htmlContent.toString(),  rawContent.toString(),  msg.getSentDate(), msg.getReceivedDate(),
					attachments.toArray(new EmailAttachment[0]));

		} catch (IOException e) {
			log.error("Failed to parse message", e);
		}
		
	}

	private void processMultipart(Multipart multiPart, StringBuffer textContent, StringBuffer htmlContent,
			List<EmailAttachment> attachments) throws IOException, MessagingException {
		for (int x = 0; x < multiPart.getCount(); x++) {
			MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
			String contentType = part.getContentType().toLowerCase();

			if (isInlineButNotEmailBodyContent(part, textContent, htmlContent)) {
				
				String id = part.getContentID();
				if(Objects.nonNull(id)) {
					if(id.startsWith("<")) {
						id = id.substring(1);
					}
					if(id.endsWith(">")) {
						id = id.substring(0, id.length()-1);
					}
					
					Document doc = Jsoup.parse(htmlContent.toString());
					Elements els = doc.getElementsByAttributeValueContaining("src", "cid:" + id);
					Element img = els.first();
					if(Objects.nonNull(img)) {
						String content = Base64.encodeBase64String(IOUtils.toByteArray(part.getInputStream()));
						img.attr("src", String.format("data:%s;base64,%s", part.getContentType(), content));
						htmlContent.setLength(0);
						htmlContent.append(doc.toString());
					} 
					
					File attachment = File.createTempFile("email", "attachment");
					OutputStream out = new FileOutputStream(attachment);
					InputStream in = part.getInputStream();
					try {
						IOUtils.copy(in, out);
					} finally {
						IOUtils.closeQuietly(out);
						IOUtils.closeQuietly(in);
					}
					attachments.add(new EmailAttachment(part.getFileName(), part.getContentType(), attachment));
				
				}
				
			} else if(Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
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
				
			} else if (contentType.startsWith("text/plain")) {
				textContent.append(part.getContent().toString());
			} else if (contentType.startsWith("text/html")) {
				htmlContent.append(part.getContent().toString());
			} else if (contentType.startsWith("multipart")) {
				processMultipart((Multipart) part.getContent(), textContent, htmlContent, attachments);
			} else {
				log.error("Missing handler in InboxProcessor processMultipart for content type {}", part.getContentType());
			}
		}
	}
	
	private boolean isInlineButNotEmailBodyContent(MimeBodyPart part, StringBuffer textContent, StringBuffer htmlContent) throws MessagingException {
		if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
			 if(part.getContentType().toLowerCase().startsWith("text/plain")) {
				 return textContent.length() > 0;
			 }
			 if(part.getContentType().toLowerCase().startsWith("text/html")) {
				 return htmlContent.length() > 0;
			 }
			 return true;
		}
		return false;
	}

	/**
	 * Returns a list of addresses in String format separated by comma
	 *
	 * @param address
	 *            an array of Address objects
	 * @return a string represents a list of addresses
	 */
	@SuppressWarnings("unused")
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
}
