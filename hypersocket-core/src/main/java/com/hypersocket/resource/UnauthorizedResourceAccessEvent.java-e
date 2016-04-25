package com.hypersocket.resource;

import com.hypersocket.session.Session;

public class UnauthorizedResourceAccessEvent extends ResourceSessionEvent {

	private static final long serialVersionUID = -7803270921133945714L;

	public static final String EVENT_RESOURCE_KEY = "unauthorized.resourceAccess";
	
	public UnauthorizedResourceAccessEvent(Object source, Resource resource, Session session) {
		super(source, EVENT_RESOURCE_KEY, false, session, resource);
	}
	
	public boolean isUsage() {
		return false;
	}


}
