package com.hypersocket.authenticator.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.session.Session;

public class AuthenticationSchemeCreatedEvent extends AuthenticationSchemeEvent {

	private static final long serialVersionUID = 743093148882904243L;
	public static final String EVENT_RESOURCE_KEY = "event.authSchemeCreated";

	public AuthenticationSchemeCreatedEvent(Object source, Session session,
			AuthenticationScheme resource, String[] moduleList) {

		super(source, EVENT_RESOURCE_KEY, true, session, resource, moduleList, null);
	}

	public AuthenticationSchemeCreatedEvent(Object source, Session session,
			Throwable e, AuthenticationScheme resource,
			String[] moduleList) {
		super(source, EVENT_RESOURCE_KEY, session, e, resource, moduleList, null);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
