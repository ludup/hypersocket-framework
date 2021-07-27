package com.hypersocket.authenticator.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.session.Session;

public class AuthenticationSchemeDeletedEvent extends AuthenticationSchemeEvent {

	private static final long serialVersionUID = 7754722381441570136L;
	public static final String EVENT_RESOURCE_KEY = "event.authSchemeDeleted";

	public AuthenticationSchemeDeletedEvent(Object source, Session session,
			AuthenticationScheme resource) {

		super(source, EVENT_RESOURCE_KEY, true, session, resource, null, null);
	}

	public AuthenticationSchemeDeletedEvent(Object source, Session session,
			Throwable e, AuthenticationScheme resource) {
		super(source, EVENT_RESOURCE_KEY, session, e, resource, null, null);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
