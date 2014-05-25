package com.hypersocket.resource;

import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class UnauthorizedResourceAccessEvent extends SessionEvent {

	private static final long serialVersionUID = -7803270921133945714L;

	public static final String EVENT_RESOURCE_KEY = "unauthorized.resourceAccess";
	
	public UnauthorizedResourceAccessEvent(Object source, Resource resource, Session session) {
		super(source, EVENT_RESOURCE_KEY, false, session);
	}
	
	public UnauthorizedResourceAccessEvent(Object source, Long resourceId,
			Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, e, session);
	}

}
