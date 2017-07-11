package com.hypersocket.password.policy.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.password.policy.PasswordPolicyResource;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.session.Session;

public class PasswordPolicyResourceEvent extends AssignableResourceEvent {

	private static final long serialVersionUID = -8797250688945804312L;

	public static final String EVENT_RESOURCE_KEY = "passwordPolicy.event";
	
	public PasswordPolicyResourceEvent(Object source, String resourceKey,
			Session session, PasswordPolicyResource resource) {
		super(source, resourceKey, true, session, resource);
	}

	public PasswordPolicyResourceEvent(Object source, String resourceKey,
			PasswordPolicyResource resource, Throwable e, Session session) {
		super(source, resourceKey, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
