package com.hypersocket.dashboard.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;
import com.hypersocket.utils.HypersocketUtils;

public class DashboardMessageEvent extends SessionEvent {

	private static final long serialVersionUID = -1438866177845416396L;
	
	public static final String EVENT_RESOURCE_KEY = "dashboardMessage.event";
	public static final String ATTR_MESSAGE_SUBJECT = "attr.messageSubject";
	public static final String ATTR_MESSAGE_BODY = "attr.messageBody";
	public static final String ATTR_MESSAGE_AUTHOR = "attr.messageAuthor";
	public static final String ATTR_MESSAGE_LINK = "attr.bodyIsLink";
	
	public DashboardMessageEvent(Object source, String resourceKey, boolean success,
			Session session, DashboardMessage message) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_MESSAGE_BODY, message.getBody());
		addAttribute(ATTR_MESSAGE_AUTHOR, message.getBody());
		addAttribute(ATTR_MESSAGE_SUBJECT, message.getName());
		addAttribute(ATTR_MESSAGE_AUTHOR, HypersocketUtils.formatDateTime(message.getExpires()));
		addAttribute(ATTR_MESSAGE_LINK, message.isBodyHyperlink());
		
	}

	public DashboardMessageEvent(Object source, String resourceKey, Throwable e,
			Session session, DashboardMessage message) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_MESSAGE_BODY, message.getBody());
		addAttribute(ATTR_MESSAGE_AUTHOR, message.getBody());
		addAttribute(ATTR_MESSAGE_SUBJECT, message.getName());
		addAttribute(ATTR_MESSAGE_AUTHOR, HypersocketUtils.formatDateTime(message.getExpires()));
		addAttribute(ATTR_MESSAGE_LINK, message.isBodyHyperlink());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
