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

	private static final long serialVersionUID = -8776154664692553132L;

	public static final String EVENT_RESOURCE_KEY = "collectMail.result";
	
	public static final String ATTR_FROM = "attr.from";
//	public static final String EVENT_REPLY_TO = "attr.replyTo";
	public static final String ATTR_TO = "attr.to";
	public static final String ATTR_CC = "attr.cc";
	public static final String ATTR_SUBJECT = "attr.subject";
	public static final String ATTR_TEXT_CONTENT = "attr.textContent";
	public static final String ATTR_HTML_CONTENT = "attr.htmlContent";
	public static final String ATTR_DATE_SENT = "attr.dateSent";
	public static final String ATTR_DATE_RECEIVED = "attr.dateReceived";
	public static final String ATTR_ATTACHMENTS = "attr.attachments";

	public CollectMailTaskResult(Object source, boolean success, Realm currentRealm, Task task, Address[] from,
			Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent, String htmlContent,
			Date sent, Date received, String... attachments) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttributes(from, replyTo, to, cc, subject, textContent, htmlContent, sent, received, attachments);
	}
	
	private void addAttributes(Address[] from,
			Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent, String htmlContent,
			Date sent, Date received, String... attachments) {
		// Not thread safe
		DateFormat rfc2113 = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");
		
		// LDP - We should be honouring reply and should use it in preference to from
		if(replyTo!=null && replyTo.length > 0) {
			addAttribute(ATTR_FROM, StringUtils.join(replyTo, '\n'));
		} else {
			addAttribute(ATTR_FROM, StringUtils.join(from, '\n'));
		}
		addAttribute(ATTR_TO, StringUtils.join(to, '\n'));
		addAttribute(ATTR_CC, StringUtils.join(cc, '\n'));
		addAttribute(ATTR_SUBJECT, StringUtils.defaultIfBlank(subject, ""));
		addAttribute(ATTR_TEXT_CONTENT, StringUtils.defaultIfBlank(textContent, ""));
		addAttribute(ATTR_HTML_CONTENT, StringUtils.defaultIfBlank(htmlContent, ""));
		addAttribute(ATTR_DATE_SENT, sent == null ? "" : rfc2113.format(sent));
		addAttribute(ATTR_DATE_RECEIVED, received == null ? "" : rfc2113.format(received));
		addAttribute(ATTR_ATTACHMENTS, StringUtils.join(attachments, ','));

	}
	

	public CollectMailTaskResult(Object source, Throwable e, Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}
	
	public CollectMailTaskResult(Object source, Throwable e, Realm currentRealm, Task task, Address[] from,
			Address[] replyTo, Address[] to, Address[] cc, String subject, String textContent, String htmlContent,
			Date sent, Date received, String... attachments) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttributes(from, replyTo, to, cc, subject, textContent, htmlContent, sent, received, attachments);
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
