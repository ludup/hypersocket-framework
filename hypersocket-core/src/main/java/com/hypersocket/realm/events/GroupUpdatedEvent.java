package com.hypersocket.realm.events;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class GroupUpdatedEvent extends GroupEvent {

	private static final long serialVersionUID = 7661773189101981651L;

	public static final String EVENT_RESOURCE_KEY = "event.groupUpdated";
	
	public static final String ATTR_PRINCIPALS_GRANTED = "attr.principalsGranted";
	public static final String ATTR_PRINCIPALS_REVOKED = "attr.principalsRevoked";
	
	private Collection<Principal> granted;
	private Collection<Principal> revoked;
	
	public GroupUpdatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			Collection<Principal> associatedPrincipals,
			Collection<Principal> granted,
			Collection<Principal> revoked) {
		super(source, "event.groupUpdated", session, realm, provider, principal,
				associatedPrincipals);
		this.granted = granted;
		this.revoked = revoked;
		addAttribute(ATTR_PRINCIPALS_GRANTED, createPrincipalList(granted));
		addAttribute(ATTR_PRINCIPALS_REVOKED, createPrincipalList(revoked));
	}

	public GroupUpdatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName, 
			Collection<Principal> associatedPrincipals) {
		super(source, "event.groupUpdated", e, session, realm, provider,
				principalName, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public Collection<Principal> getGranted() {
		return granted;
	}
	
	public Collection<Principal> getRevoked() {
		return revoked;
	}

}
