package com.hypersocket.role.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RoleDeletedEvent extends RoleEvent {

	private static final long serialVersionUID = -8532347445112592883L;

	public static final String EVENT_RESOURCE_KEY = "role.deleted";

	public RoleDeletedEvent(Object source, Session session, Realm realm,
			Role resource) {
		super(source, EVENT_RESOURCE_KEY, session, realm, resource);
	}

	public RoleDeletedEvent(Object source, String roleName, Throwable e,
			Session session, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, roleName, e, session, realm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
