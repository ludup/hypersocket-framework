package com.hypersocket.delegation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.session.Session;

public class UserDelegationResourceEvent extends AssignableResourceEvent {

//	public static final String ATTR_NAME = "attr.name";

	private static final long serialVersionUID = -7917432357033828073L;

	public static final String EVENT_RESOURCE_KEY = "userDelegation.event";
	
	public UserDelegationResourceEvent(Object source, String resourceKey,
			Session session, UserDelegationResource resource) {
		super(source, resourceKey, true, session, resource);

		/**
		 * TODO add attributes of your resource here. Make sure all attributes
		 * have a constant string definition like the commented out example above,
		 * its important for its name to start with ATTR_ as this is picked up during 
		 * the registration process
		 */
	}

	public UserDelegationResourceEvent(Object source, String resourceKey,
			UserDelegationResource resource, Throwable e, Session session) {
		super(source, resourceKey, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
