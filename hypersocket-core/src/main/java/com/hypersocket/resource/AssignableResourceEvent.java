package com.hypersocket.resource;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.hypersocket.session.Session;

public abstract class AssignableResourceEvent extends ResourceEvent {

	private static final long serialVersionUID = -81919926673011642L;

	public static final String ATTR_ROLES = "attr.roles";

	public AssignableResourceEvent(Object source, String resourceKey,
			boolean success, Session session, AssignableResource resource) {
		super(source, resourceKey, success, session, resource);
		addRoleAttribute(resource);
	}

	public AssignableResourceEvent(Object source, String resourceKey,
			AssignableResource resource, Throwable e, Session session) {
		super(source, resourceKey, resource, e, session);
		addRoleAttribute(resource);
	}

	private void addRoleAttribute(AssignableResource resource) {
		addAttribute(ATTR_ROLES,
				StringUtils.collectionToCommaDelimitedString(resource
						.getRoles()));
	}
}
