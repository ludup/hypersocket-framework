package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class RealmUpdatedEvent extends RealmEvent {

	private static final long serialVersionUID = -237151176246845100L;

	public static final String ATTR_OLD_REALM_NAME = "attr.oldRealmName";
	
	public static final String EVENT_RESOURCE_KEY = "event.realmUpdated";
	
	public RealmUpdatedEvent(Object source, Session session, String oldName, Realm realm) {
		super(source, "event.realmUpdated", true, session, realm);
		addAttribute(ATTR_OLD_REALM_NAME, oldName);
	}

	public RealmUpdatedEvent(Object source, Throwable e,
			Session session, Realm realmName) {
		super(source, "event.realmUpdated", e, session, realmName);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
