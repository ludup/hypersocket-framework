package com.hypersocket.email.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.email.EmailNotificationServiceImpl;
import com.hypersocket.email.EmailReceipt;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.utils.HypersocketUtils;

public class EmailOpenedEvent extends SystemEvent {

	private static final long serialVersionUID = -664923679180483321L;

	public static final String EVENT_RESOURCE_KEY = "emailOpened.event";
	
	public static final String ATTR_EMAIL = "attr.email";
	public static final String ATTR_SUBJECT = "attr.subject";
	public static final String ATTR_PRINCIPAL = "attr.principalName";
	public static final String ATTR_PRINCIPAL_DESC = "attr.principalDesc";
	public static final String ATTR_EMAIL_SENT = "attr.emailSent";
	public static final String ATTR_EMAIL_READ = "attr.emailRead";
	
	public EmailOpenedEvent(Object source, EmailReceipt receipt, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
		addAttribute(ATTR_EMAIL, receipt.getEmailAddress());
		addAttribute(ATTR_SUBJECT, receipt.getTracker().getSubject());
		addAttribute(ATTR_EMAIL_SENT, HypersocketUtils.formatDateTime(receipt.getCreateDate()));
		addAttribute(ATTR_EMAIL_READ, HypersocketUtils.formatDateTime(receipt.getOpened()));
		addAttribute(ATTR_PRINCIPAL, receipt.getPrincipal()==null ? "" : receipt.getPrincipal().getName());
		addAttribute(ATTR_PRINCIPAL_DESC, receipt.getPrincipal()==null ? "" : receipt.getPrincipal().getPrincipalDescription());
	}

	public EmailOpenedEvent(Object source, Throwable e, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm);
	}



	@Override
	public String getResourceBundle() {
		return EmailNotificationServiceImpl.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
