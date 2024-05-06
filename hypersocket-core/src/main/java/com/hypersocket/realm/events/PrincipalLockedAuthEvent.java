package com.hypersocket.realm.events;

import com.hypersocket.realm.Principal;

public class PrincipalLockedAuthEvent extends AbstractPrincipalLockedEvent {


	private static final long serialVersionUID = 4373425682304591292L;
	
	public static final String EVENT_RESOURCE_KEY = "event.principalLockedAuth";


	public PrincipalLockedAuthEvent(Object source, Principal principal,
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, principal, lockedPrincipal);
		
	}
	
	public PrincipalLockedAuthEvent(Object source, Throwable e, Principal principal, 
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, e, principal, lockedPrincipal);
	}


}
