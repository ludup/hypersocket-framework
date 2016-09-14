package com.hypersocket.resource;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.permissions.Role;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public abstract class AssignableResourceEvent extends ResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "assignable.event";

	private static final long serialVersionUID = -81919926673011642L;

	public static final String ATTR_ROLES = "attr.roles";
	public static final String ATTR_ASSIGNED_ROLES = "attr.assignedRoles";
	public static final String ATTR_UNASSIGNED_ROLES = "attr.unassignedRoles";

	Collection<Role> unassignedRoles;
	Collection<Role> assignedRoles;

	public AssignableResourceEvent(Object source, String resourceKey,
								   boolean success, Session session, AssignableResource resource) {
		super(source, resourceKey, success, session, resource);
		addRoleAttribute(resource);
		assignedRoles = resource.getAssignedRoles();
		unassignedRoles = resource.getUnassignedRoles();
		addAttribute(ATTR_ASSIGNED_ROLES, createRoleList(assignedRoles == null ? Collections.<Role>emptyList() : assignedRoles));
		addAttribute(ATTR_UNASSIGNED_ROLES, createRoleList(unassignedRoles == null ? Collections.<Role>emptyList() : unassignedRoles));
	}

	public AssignableResourceEvent(Object source, String resourceKey,
								   AssignableResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		addRoleAttribute(resource);
		assignedRoles = resource.getAssignedRoles();
		unassignedRoles = resource.getUnassignedRoles();
		addAttribute(ATTR_ASSIGNED_ROLES, createRoleList(assignedRoles == null ? Collections.<Role>emptyList() : assignedRoles));
		addAttribute(ATTR_UNASSIGNED_ROLES, createRoleList(unassignedRoles == null ? Collections.<Role>emptyList() : unassignedRoles));
	}

	private void addRoleAttribute(AssignableResource resource) {
		addAttribute(ATTR_ROLES,
				createRoleList(resource.getRoles()));
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private String createRoleList(Collection<Role> roles) {
		StringBuffer buf = new StringBuffer();
		for(Role r : roles) {
			if(buf.length() > 0) {
				buf.append("\r\n");
			}
			buf.append(r.getName());
		}
		return buf.toString();
	}
}
