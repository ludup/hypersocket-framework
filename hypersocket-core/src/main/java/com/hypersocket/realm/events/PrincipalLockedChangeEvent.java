package com.hypersocket.realm.events;

import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;

public class PrincipalLockedChangeEvent extends AbstractPrincipalLockedEvent {

	private static final long serialVersionUID = 8984021801869214379L;

	public static final String EVENT_RESOURCE_KEY = "event.principalLockedChange";

	
	public PrincipalLockedChangeEvent(Object source, Session session, Principal principal, 
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, session, principal, lockedPrincipal);
	}
	
	public PrincipalLockedChangeEvent(Object source, Throwable e, Session session, Principal principal, 
			Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, e, session, principal, lockedPrincipal);
	}


}
