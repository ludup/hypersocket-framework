package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserDeletedEvent extends UserEvent {

	private static final long serialVersionUID = 2300774900816788017L;

	public static final String EVENT_RESOURCE_KEY = "event.userDeleted";
	
	public UserDeletedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal) {
		super(source, "event.userDeleted", session, realm, provider, principal);
	}

	public UserDeletedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName) {
		super(source, "event.userDeleted", e, session, realm, provider,
				principalName);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
