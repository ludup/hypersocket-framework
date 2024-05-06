package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmServiceImpl;

public abstract class AbstractPrincipalLockedEvent extends SystemEvent {

	private static final long serialVersionUID = 3984021800869214379L;

	public static final String ATTR_LOCKED_PRINCIPAL_NAME = "attr.lockedPrincipalName";
	public static final String ATTR_LOCKED_PRINCIPAL_REALM_NAME = "attr.lockedPrincipalRealmName";
	public static final String ATTR_LOCKED_PRINCIPAL_REALM_TYPE = "attr.lockedPrincipalRealmType";

	private Principal lockedPrincipal;
	private String eventKey;
	
	public AbstractPrincipalLockedEvent(String eventKey, Object source, Principal principal, 
			Principal lockedPrincipal) {
		super(source, eventKey, true, principal.getRealm());
		this.lockedPrincipal = lockedPrincipal;
		this.eventKey = eventKey;
		addAttributes(lockedPrincipal);
	}

	public AbstractPrincipalLockedEvent(String eventKey, Object source, Throwable e, Principal principal, 
			Principal lockedPrincipal) {
		super(source, eventKey, e, principal.getRealm());
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
	
	@Override
	public String getResourceBundle() {
		return RealmServiceImpl.RESOURCE_BUNDLE;
	}

	private void addAttributes(Principal lockedPrincipal) {
		addAttribute(ATTR_LOCKED_PRINCIPAL_NAME, lockedPrincipal.getName());
		addAttribute(ATTR_LOCKED_PRINCIPAL_REALM_NAME, lockedPrincipal.getRealmName());
		addAttribute(ATTR_LOCKED_PRINCIPAL_REALM_TYPE, lockedPrincipal.getRealmModule());
	}
	
}
