package com.hypersocket.realm.events;

import com.hypersocket.session.Session;

public class RealmDeletedEvent extends RealmEvent {

	private static final long serialVersionUID = -237151176246845100L;

	public static final String EVENT_RESOURCE_KEY = "event.realmDeleted";
	
	public RealmDeletedEvent(Object source, Session session, String realmName) {
		super(source, "event.realmDeleted", true, session, realmName);
	}

	public RealmDeletedEvent(Object source, Throwable e,
			Session session, String realmName) {
		super(source, "event.realmDeleted", e, session, realmName);
	}

}
