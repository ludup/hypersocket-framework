package com.hypersocket.inbox.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class CollectMailTaskResult extends AbstractTaskResult {

	public static final String EVENT_RESOURCE_KEY = "collectMail.result";
	public static final String EVENT_FROM = "collectMail.from";
	public static final String EVENT_REPLY_TO = "collectMail.replyTo";
	public static final String EVENT_TO = "collectMail.to";
	public static final String EVENT_CC = "collectMail.cc";
	public static final String EVENT_SUBJECT = "collectMail.subject";
	public static final String EVENT_TEXT_CONTENT = "collectMail.textContent";
	public static final String EVENT_HTML_CONTENT = "collectMail.htmlContent";
	public static final String EVENT_DATE_SENT = "collectMail.dateSent";
	public static final String EVENT_DATE_RECEIVED = "collectMail.dateReceived";
	public static final String EVENT_ATTACHMENTS = "collectMail.attachments";

	public CollectMailTaskResult(Object source, boolean success, Realm currentRealm, Task task, Address[] from,
			Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent, String htmlContent,
			Date sent, Date received, String... attachments) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);

		// Not thread safe
		DateFormat rfc2113 = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");

		addAttribute(EVENT_FROM, StringUtils.join(from, ','));
		addAttribute(EVENT_REPLY_TO, StringUtils.join(replyTo, ','));
		addAttribute(EVENT_TO, StringUtils.join(to, ','));
		addAttribute(EVENT_CC, StringUtils.join(cc, ','));
		addAttribute(EVENT_SUBJECT, StringUtils.defaultIfBlank(subject, ""));
		addAttribute(EVENT_TEXT_CONTENT, StringUtils.defaultIfBlank(textContent, ""));
		addAttribute(EVENT_HTML_CONTENT, StringUtils.defaultIfBlank(htmlContent, ""));
		addAttribute(EVENT_DATE_SENT, sent == null ? "" : rfc2113.format(sent));
		addAttribute(EVENT_DATE_RECEIVED, received == null ? "" : rfc2113.format(received));
		addAttribute(EVENT_ATTACHMENTS, StringUtils.join(attachments, ','));

	}

	public CollectMailTaskResult(Object source, Throwable e, Realm currentRealm, Task task, Address[] from,
			Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent, String htmlContent,
			Date sent, Date received, String... attachments) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return CollectMailTask.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
