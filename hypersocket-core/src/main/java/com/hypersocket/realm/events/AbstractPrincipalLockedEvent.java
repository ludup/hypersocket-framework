package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;

public abstract class AbstractPrincipalLockedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021800869214379L;

	public static final String ATTR_LOCKED_PRINCIPAL = "attr.lockedPrincipal";
	public static final String ATTR_LOCKED_PRINCIPAL_NAME = "attr.lockedPrincipalName";
	public static final String ATTR_LOCKED_PRINCIPAL_REALM_NAME = "attr.lockedPrincipalRealmName";
	public static final String ATTR_LOCKED_PRINCIPAL_REALM = "attr.lockedPrincipalRealm";
	public static final String ATTR_LOCKED_PRINCIPAL_REALM_TYPE = "attr.lockedPrincipalRealmType";

	private Principal lockedPrincipal;
	private String eventKey;
	
	public AbstractPrincipalLockedEvent(String eventKey, Object source, Session session, Principal principal, 
			Principal lockedPrincipal) {
		super(source, eventKey, session, principal.getRealm(), null, principal);
		this.lockedPrincipal = lockedPrincipal;
		this.eventKey = eventKey;
		addAttributes(lockedPrincipal);
	}

	public AbstractPrincipalLockedEvent(String eventKey, Object source, Throwable e, Session session, Principal principal, 
			Principal lockedPrincipal) {
		super(source, eventKey, e, session, principal.getRealm(), null, principal.getName());
		this.lockedPrincipal = lockedPrincipal;
		this.eventKey = eventKey;
		addAttributes(lockedPrincipal);
	}
	
	public Principal getLockedPrincipal() {
		return lockedPrincipal;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), this.eventKey);
	}

	private void addAttributes(Principal lockedPrincipal) {
		addAttribute(ATTR_LOCKED_PRINCIPAL, lockedPrincipal.getId());
		addAttribute(ATTR_LOCKED_PRINCIPAL_NAME, lockedPrincipal.getName());
		addAttribute(ATTR_LOCKED_PRINCIPAL_REALM, lockedPrincipal.getRealm().getId());
		addAttribute(ATTR_LOCKED_PRINCIPAL_REALM_NAME, lockedPrincipal.getRealmName());
		addAttribute(ATTR_LOCKED_PRINCIPAL_REALM_TYPE, lockedPrincipal.getRealmModule());
	}
	
}
