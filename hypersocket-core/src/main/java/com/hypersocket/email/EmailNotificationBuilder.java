package com.hypersocket.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.hypersocket.messagedelivery.MessageDeliveryBuilder;

public abstract class EmailNotificationBuilder extends MessageDeliveryBuilder {
	private String subject;
	private String html;
	private boolean archive;
	private boolean track;
	private List<EmailAttachment> attachments = new ArrayList<>();
	private String replyToName;
	private String replyToEmail;

	public EmailNotificationBuilder() {
	}

	public String replyToName() {
		return replyToName;
	}

	public EmailNotificationBuilder replyTo(String replyToName, String replyToEmail) {
		replyToName(replyToName);
		replyToEmail(replyToEmail);
		return this;
	}

	public EmailNotificationBuilder replyToName(String replyToName) {
		this.replyToName = replyToName;
		return this;
	}

	public String replyToEmail() {
		return replyToEmail;
	}

	public EmailNotificationBuilder replyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
		return this;
	}

	public String subject() {
		return subject;
	}

	public EmailNotificationBuilder subject(String subject) {
		this.subject = subject;
		return this;
	}

	public String html() {
		return html;
	}

	public EmailNotificationBuilder html(String html) {
		this.html = html;
		return this;
	}

	public boolean archive() {
		return archive;
	}

	public EmailNotificationBuilder archive(boolean archive) {
		this.archive = archive;
		return this;
	}

	public boolean track() {
		return track;
	}

	public EmailNotificationBuilder track(boolean track) {
		this.track = track;
		return this;
	}

	public List<EmailAttachment> attachments() {
		return attachments;
	}

	public EmailNotificationBuilder attachment(EmailAttachment attachment) {
		this.attachments.clear();
		this.attachments.add(attachment);
		return this;
	}

	public EmailNotificationBuilder addAttachments(List<EmailAttachment> attachments) {
		this.attachments.addAll(attachments);
		return this;
	}

	public EmailNotificationBuilder addAttachments(EmailAttachment... attachments) {
		addAttachments(Arrays.asList(attachments));
		return this;
	}

	public EmailNotificationBuilder attachments(List<EmailAttachment> attachments) {
		this.attachments.clear();
		this.attachments.addAll(attachments);
		return this;
	}


	@Override
	public String getName(String val) {

		var p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);
		var m = p.matcher(val);

		if (m.find()) {
			return m.group(1).replaceAll("[\\n\\r]+", "");
		}

		return "";
	}

	@Override
	public String getAddress(String val) {
		var p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);
		var m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			var name = m.group(1).replaceAll("[\\n\\r]+", "");
			var email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return email;
			} else {
				throw new IllegalArgumentException();
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return val;
		}

		throw new IllegalArgumentException();
	}
}
