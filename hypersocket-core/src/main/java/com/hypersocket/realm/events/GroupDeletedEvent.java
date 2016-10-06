package com.hypersocket.realm.events;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class GroupDeletedEvent extends GroupEvent {

	private static final long serialVersionUID = 2338593199636073428L;

	public static final String EVENT_RESOURCE_KEY = "event.groupDeleted";
	
	public GroupDeletedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal, Collection<Principal> associatedPrincipals) {
		super(source, "event.groupDeleted", session, realm, provider, principal, associatedPrincipals);
	}

	public GroupDeletedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName, Collection<Principal> associatedPrincipals) {
		super(source, "event.groupDeleted", e, session, realm, provider,
				principalName, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
