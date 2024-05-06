package com.hypersocket.realm.events;

import com.hypersocket.realm.Principal;

public class PrincipalLockedChangeEvent extends AbstractPrincipalLockedEvent {

	private static final long serialVersionUID = 8984021801869214379L;

	public static final String EVENT_RESOURCE_KEY = "event.principalLockedChange";

	
	public PrincipalLockedChangeEvent(Object source, Principal principal, 
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, principal, lockedPrincipal);
	}
	
	public PrincipalLockedChangeEvent(Object source, Throwable e, Principal principal, 
			Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, e, principal, lockedPrincipal);
	}


}
