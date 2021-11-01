package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.session.Session;

public class SessionOpenEvent extends SessionStateEvent {

	private static final long serialVersionUID = 6561037201424298867L;

	public static final String EVENT_RESOURCE_KEY = "session.opened";
	
	public static final String ATTR_USER_AGENT = "attr.userAgent";
	public static final String ATTR_USER_AGENT_VERSION = "attr.userAgentVersion";
	public static final String ATTR_OS = "attr.os";
	public static final String ATTR_OS_VERSION = "attr.osVersion";
	public static final String ATTR_TRANSIENT = "attr.transient";
	
	public SessionOpenEvent(Object source, Session session) {
		super(source, EVENT_RESOURCE_KEY, true, session);
		addAttribute(ATTR_USER_AGENT, session.getUserAgent());
		addAttribute(ATTR_USER_AGENT_VERSION, session.getUserAgentVersion());
		addAttribute(ATTR_OS, session.getOs());
		addAttribute(ATTR_OS_VERSION, session.getOsVersion());
		addAttribute(ATTR_TRANSIENT, session.isTransient());
		
		/* Ignore local-api calls only */
		if(isLocalApiEvent(session)) {
			hidden = true;
		}
	}
	
	@Override
	public boolean isHidden() {
		return super.isHidden();
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
