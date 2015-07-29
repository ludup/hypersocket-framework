package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserUpdatedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.userUpdated";
	
	public UserUpdatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<? extends Principal> associatedPrincipals, Map<String,String> properties) {
		super(source, "event.userUpdated", session, realm, provider, principal,
				associatedPrincipals, properties);
	}

	public UserUpdatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties, List<? extends Principal> associatedPrincipals) {
		super(source, "event.userUpdated", e, session, realm, provider,
				principalName, properties, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
