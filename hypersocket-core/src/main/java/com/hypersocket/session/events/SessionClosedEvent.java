package com.hypersocket.session.events;

import com.hypersocket.session.Session;

public class SessionClosedEvent extends SessionEvent {

	private static final long serialVersionUID = -4449174449795883302L;

	public static final String EVENT_RESOURCE_KEY = "session.closed";
	
	public SessionClosedEvent(Object source,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, true, session);
	}

	
	
}
