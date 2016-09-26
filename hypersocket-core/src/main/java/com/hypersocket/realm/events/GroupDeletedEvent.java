package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class GroupDeletedEvent extends GroupEvent {

	private static final long serialVersionUID = 2338593199636073428L;

	public static final String EVENT_RESOURCE_KEY = "event.groupDeleted";
	
	public GroupDeletedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal) {
		super(source, "event.groupDeleted", session, realm, provider, principal);
	}

	public GroupDeletedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName) {
		super(source, "event.groupDeleted", e, session, realm, provider,
				principalName);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
