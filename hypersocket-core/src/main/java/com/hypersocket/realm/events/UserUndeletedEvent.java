package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserUndeletedEvent extends UserEvent {

	private static final long serialVersionUID = 2300774900816782017L;

	public static final String EVENT_RESOURCE_KEY = "event.userUndeleted";
	
	public UserUndeletedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal) {
		super(source, "event.userUndeleted", session, realm, provider, principal);
	}

	public UserUndeletedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName) {
		super(source, "event.userUndeleted", e, session, realm, provider,
				principalName);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
