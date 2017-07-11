package com.hypersocket.password.policy.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.password.policy.PasswordPolicyResource;
import com.hypersocket.session.Session;

public class PasswordPolicyResourceDeletedEvent extends
		PasswordPolicyResourceEvent {

	private static final long serialVersionUID = 2935081412226807092L;

	public static final String EVENT_RESOURCE_KEY = "passwordPolicy.deleted";

	public PasswordPolicyResourceDeletedEvent(Object source,
			Session session, PasswordPolicyResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public PasswordPolicyResourceDeletedEvent(Object source,
			PasswordPolicyResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
