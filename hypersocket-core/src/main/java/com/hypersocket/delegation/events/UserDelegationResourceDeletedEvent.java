package com.hypersocket.delegation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.session.Session;

public class UserDelegationResourceDeletedEvent extends
		UserDelegationResourceEvent {

	private static final long serialVersionUID = 935298288020500867L;

	public static final String EVENT_RESOURCE_KEY = "userDelegation.deleted";

	public UserDelegationResourceDeletedEvent(Object source,
			Session session, UserDelegationResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public UserDelegationResourceDeletedEvent(Object source,
			UserDelegationResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
