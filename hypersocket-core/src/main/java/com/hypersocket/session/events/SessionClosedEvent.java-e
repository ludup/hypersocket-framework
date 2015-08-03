package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.session.Session;

public class SessionClosedEvent extends SessionStateEvent {

	private static final long serialVersionUID = -4449174449795883302L;

	public static final String EVENT_RESOURCE_KEY = "session.closed";
	
	public SessionClosedEvent(Object source,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, true, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
}
