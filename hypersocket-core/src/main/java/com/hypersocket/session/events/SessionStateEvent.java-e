package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.session.Session;

public class SessionStateEvent extends SessionEvent {

	private static final long serialVersionUID = -593306796407403759L;

	public static final String EVENT_RESOURCE_KEY = "session.stateEvent";
	
	public SessionStateEvent(Object source, String resourceKey, boolean success, Session session) {
		super(source, resourceKey, success, session);
	}

	public SessionStateEvent(Object source, String resourceKey, Throwable e, Session session) {
		super(source, resourceKey, e, session);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
