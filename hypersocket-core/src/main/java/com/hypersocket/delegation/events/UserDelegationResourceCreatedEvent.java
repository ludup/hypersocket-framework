package com.hypersocket.delegation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.session.Session;

public class UserDelegationResourceCreatedEvent extends
		UserDelegationResourceEvent {

	private static final long serialVersionUID = 8131355529671954002L;

	public static final String EVENT_RESOURCE_KEY = "userDelegation.created";
	
	public UserDelegationResourceCreatedEvent(Object source,
			Session session,
			UserDelegationResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public UserDelegationResourceCreatedEvent(Object source,
			UserDelegationResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
