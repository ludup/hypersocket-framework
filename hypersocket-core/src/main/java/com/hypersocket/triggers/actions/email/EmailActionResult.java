package com.hypersocket.triggers.actions.email;

import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class EmailActionResult extends ActionResult {

	private static final long serialVersionUID = -5374654828955586879L;

	public static final String EVENT_RESOURCE_KEY = "event.sentEmail";

	public static final String ATTR_ACTION_NAME = "attr.actionName";
	public static final String ATTR_TRIGGER_NAME = "attr.triggerName";
	public static final String ATTR_SUBJECT = "attr.subject";
	public static final String ATTR_BODY = "attr.body";
	public static final String ATTR_TO = "attr.to";
	public static final String ATTR_CC = "attr.cc";
	public static final String ATTR_BCC = "attr.bcc";

	public EmailActionResult(Object source, Realm currentRealm,
			String actionName, String triggerName, String subject, String body,
			String to, String cc, String bcc) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
		addAttributes(actionName, triggerName, subject, body, to, cc, bcc);
	}

	private void addAttributes(String actionName, String triggerName,
			String subject, String body, String to, String cc, String bcc) {
		addAttribute(ATTR_TRIGGER_NAME, triggerName);
		addAttribute(ATTR_ACTION_NAME, actionName);
		addAttribute(ATTR_SUBJECT, subject);
		addAttribute(ATTR_BODY, body);
		addAttribute(ATTR_TO, to);
		addAttribute(ATTR_CC, cc);
		addAttribute(ATTR_BCC, bcc);

	}

	public EmailActionResult(Object source, Throwable e, Realm currentRealm,
			String actionName, String triggerName, String subject, String body,
			String to, String cc, String bcc) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm);
		addAttributes(actionName, triggerName, subject, body, to, cc, bcc);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

}
