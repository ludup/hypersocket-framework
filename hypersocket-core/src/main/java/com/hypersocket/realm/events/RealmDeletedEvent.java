package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RealmDeletedEvent extends RealmEvent {

	private static final long serialVersionUID = -237151176246845100L;

	public static final String EVENT_RESOURCE_KEY = "event.realmDeleted";
	
	public RealmDeletedEvent(Object source, Session session, Realm realm) {
		super(source, "event.realmDeleted", true, session, realm);
	}

	public RealmDeletedEvent(Object source, Throwable e,
			Session session, Realm realm) {
		super(source, "event.realmDeleted", e, session, realm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
