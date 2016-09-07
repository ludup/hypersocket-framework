package com.hypersocket.role.events;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RoleUpdatedEvent extends RoleEvent {

	private static final long serialVersionUID = 3411262639541921403L;

	public static final String EVENT_RESOURCE_KEY = "role.updated";

	public static final String ATTR_PRINCIPALS_GRANTED = "attr.principalsGranted";
	public static final String ATTR_PRINCIPALS_REVOKED = "attr.principalsRevoked";
	
	Collection<Principal> granted;
	Collection<Principal> revoked;
	
	public RoleUpdatedEvent(Object source, Session session, Realm realm,
			Role resource, Collection<Principal> granted, Collection<Principal> revoked) {
		super(source, EVENT_RESOURCE_KEY, session, realm, resource);
		this.granted = granted;
		this.revoked = revoked;
		addAttribute(ATTR_PRINCIPALS_GRANTED, createPrincipalList(granted));
		addAttribute(ATTR_PRINCIPALS_REVOKED, createPrincipalList(revoked));
	}

	public RoleUpdatedEvent(Object source, String roleName, Throwable e,
			Session session, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, roleName, e, session, realm);
	}
	
	public Collection<Principal> getGranted() {
		return granted;
	}
	
	public Collection<Principal> getRevoked() {
		return revoked;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
