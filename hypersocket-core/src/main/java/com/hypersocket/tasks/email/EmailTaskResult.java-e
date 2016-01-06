package com.hypersocket.tasks.email;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class EmailTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = -5374654828955586879L;

	public static final String EVENT_RESOURCE_KEY = "event.sentEmail";

	public static final String ATTR_SUBJECT = "attr.subject";
	public static final String ATTR_BODY = "attr.body";
	public static final String ATTR_TO = "attr.to";
	public static final String ATTR_CC = "attr.cc";
	public static final String ATTR_BCC = "attr.bcc";

	public EmailTaskResult(Object source, Realm currentRealm,
			Task task, String subject, String body, String to,
			String cc, String bcc) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm, task);
		addAttributes(subject, body, to, cc, bcc);
	}

	private void addAttributes(String subject, String body, String to,
			String cc, String bcc) {
		addAttribute(ATTR_SUBJECT, subject);
		addAttribute(ATTR_BODY, body);
		addAttribute(ATTR_TO, to);
		addAttribute(ATTR_CC, cc);
		addAttribute(ATTR_BCC, bcc);

	}

	public EmailTaskResult(Object source, Throwable e, Realm currentRealm,
			Task task, String subject, String body, String to,
			String cc, String bcc) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttributes(subject, body, to, cc, bcc);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
