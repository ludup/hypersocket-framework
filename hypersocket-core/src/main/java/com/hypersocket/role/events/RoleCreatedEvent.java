package com.hypersocket.role.events;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RoleCreatedEvent extends RoleEvent {

	private static final long serialVersionUID = -9019703621644064623L;

	public static final String EVENT_RESOURCE_KEY = "role.created";
	public static final String ATTR_PRINCIPALS_GRANTED = "attr.principalsGranted";
	Collection<Principal> granted;
	
	public RoleCreatedEvent(Object source, Session session, Realm realm,
			Role resource, Collection<Principal> granted) {
		super(source, EVENT_RESOURCE_KEY, session, realm, resource);
		this.granted = granted;
		addAttribute(ATTR_PRINCIPALS_GRANTED, createPrincipalList(granted));
	}

	public RoleCreatedEvent(Object source, String roleName, Throwable e,
			Session session, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, roleName, e, session, realm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public Collection<Principal> getGranted() {
		return granted;
	}

}
