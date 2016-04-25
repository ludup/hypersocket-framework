package com.hypersocket.realm.events;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class ProfileUpdatedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.profileUpdated";
	
	public ProfileUpdatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			Map<String, String> properties) {
		super(source, "event.profileUpdated", session, realm, provider, principal,
				null, properties);
	}

	public ProfileUpdatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties) {
		super(source, "event.profileUpdated", e, session, realm, provider,
				principalName, properties, null);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
