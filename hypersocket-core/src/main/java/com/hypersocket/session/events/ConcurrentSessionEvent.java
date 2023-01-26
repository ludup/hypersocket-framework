package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.session.Session;

public class ConcurrentSessionEvent extends SessionStateEvent {

	private static final long serialVersionUID = 6561037201024298067L;

	public static final String EVENT_RESOURCE_KEY = "concurrent.session";
	
	public static final String ATTR_CONCURRENT_SESSION_PRINCIPAL = "attr.concurrent.session.principalName";
	public static final String ATTR_CONCURRENT_SESSION_TRACKED = "attr.concurrent.session.tracked.info";
	
	public ConcurrentSessionEvent(Object source, Session session, String sessionTrackedInfo) {
		super(source, EVENT_RESOURCE_KEY, true, session);
		addAttribute(ATTR_CONCURRENT_SESSION_PRINCIPAL, session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_CONCURRENT_SESSION_TRACKED, sessionTrackedInfo);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
