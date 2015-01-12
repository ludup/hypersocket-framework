package com.hypersocket.resource;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public abstract class AssignableResourceEvent extends ResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "assignable.event";
	
	private static final long serialVersionUID = -81919926673011642L;

	public static final String ATTR_ROLES = "attr.roles";

	public AssignableResourceEvent(Object source, String resourceKey,
			boolean success, Session session, AssignableResource resource) {
		super(source, resourceKey, success, session, resource);
		addRoleAttribute(resource);
	}

	public AssignableResourceEvent(Object source, String resourceKey,
			AssignableResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		addRoleAttribute(resource);
	}

	private void addRoleAttribute(AssignableResource resource) {
		addAttribute(ATTR_ROLES,
				ResourceUtils.createCommaSeparatedString(
						resource.getRoles()));
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
