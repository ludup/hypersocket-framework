package com.hypersocket.realm.events;

import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RealmCreatedEvent extends RealmEvent {

	private static final long serialVersionUID = -237151176246845100L;

	public RealmCreatedEvent(Object source, Session session, Realm realm) {
		super(source, "event.realmCreated", true, session, realm);
	}

	public RealmCreatedEvent(Object source, Throwable e,
			Session session, String realmName) {
		super(source, "event.realmCreated", e, session, realmName);
	}

}
