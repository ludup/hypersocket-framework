package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RealmCreatedEvent extends RealmEvent {

	private static final long serialVersionUID = -237151176246845100L;

	public static final String EVENT_RESOURCE_KEY = "event.realmCreated";
	
	public RealmCreatedEvent(Object source, Session session, Realm realm) {
		super(source, "event.realmCreated", true, session, realm);
	}

	public RealmCreatedEvent(Object source, Throwable e,
			Session session, String realmName, String realmType) {
		super(source, "event.realmCreated", e, session, realmName, realmType);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
