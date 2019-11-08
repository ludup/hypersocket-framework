package com.hypersocket.email.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class EmailEvent extends SystemEvent {

	private static final long serialVersionUID = -5374654828955586879L;

	public static final String EVENT_RESOURCE_KEY = "event.sentEmail";

	public static final String ATTR_SUBJECT = "attr.subject";
	public static final String ATTR_BODY = "attr.body";
	public static final String ATTR_TO = "attr.to";
	public static final String ATTR_CONTEXT = "attr.context";

	public EmailEvent(Object source, Realm currentRealm, Task task, String subject, String body, String to, String context) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
		addAttributes(subject, body, to, context);
	}

	public EmailEvent(Object source, Realm currentRealm, String subject, String body, String to, String context) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
		addAttributes(subject, body, to, context);
	}

	private void addAttributes(String subject, String body, String to, String context) {
		addAttribute(ATTR_SUBJECT, subject);
		addAttribute(ATTR_BODY, body);
		addAttribute(ATTR_TO, to);
		addAttribute(ATTR_CONTEXT, context);

	}

	public EmailEvent(Object source, Throwable e, Realm currentRealm, String subject, String body, String to, String context) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm);
		addAttributes(subject, body, to, context);
	}
	
	public String getTo() {
		return getAttribute(ATTR_TO);
	}
	
	public String getContext() {
		return getAttribute(ATTR_CONTEXT);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
