package com.hypersocket.realm.events;

import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;

public class PrincipalLockedAuthEvent extends AbstractPrincipalLockedEvent {


	private static final long serialVersionUID = 4373425682304591292L;
	
	public static final String EVENT_RESOURCE_KEY = "event.principalLockedAuth";


	public PrincipalLockedAuthEvent(Object source, Session session, Principal principal,
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, session, principal, lockedPrincipal);
		
	}
	
	public PrincipalLockedAuthEvent(Object source, Throwable e, Session session, Principal principal, 
				Principal lockedPrincipal) {
		super(EVENT_RESOURCE_KEY, source, e, session, principal, lockedPrincipal);
	}


}
