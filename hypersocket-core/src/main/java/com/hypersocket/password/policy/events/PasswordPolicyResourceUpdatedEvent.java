package com.hypersocket.password.policy.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.password.policy.PasswordPolicyResource;
import com.hypersocket.session.Session;

public class PasswordPolicyResourceUpdatedEvent extends
		PasswordPolicyResourceEvent {

	private static final long serialVersionUID = 1L;

	public static final String EVENT_RESOURCE_KEY = "passwordPolicy.updated";

	public PasswordPolicyResourceUpdatedEvent(Object source,
			Session session, PasswordPolicyResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public PasswordPolicyResourceUpdatedEvent(Object source,
			PasswordPolicyResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
