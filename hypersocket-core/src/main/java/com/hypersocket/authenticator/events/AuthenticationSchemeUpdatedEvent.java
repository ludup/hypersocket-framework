package com.hypersocket.authenticator.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.session.Session;

public class AuthenticationSchemeUpdatedEvent extends AuthenticationSchemeEvent {

	private static final long serialVersionUID = -8280013679597726000L;
	public static final String EVENT_RESOURCE_KEY = "event.authSchemeUpdated";

	public AuthenticationSchemeUpdatedEvent(Object source, Session session,
			AuthenticationScheme resource, String[] moduleList, String[] oldModuleList) {

		super(source, EVENT_RESOURCE_KEY, true, session, resource, moduleList, oldModuleList);
	}

	public AuthenticationSchemeUpdatedEvent(Object source, Session session,
			Throwable e, AuthenticationScheme resource,
			String[] moduleList, String[] oldModuleList) {
		super(source, EVENT_RESOURCE_KEY, session, e, resource, moduleList, oldModuleList);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
