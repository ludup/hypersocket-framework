package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class PasswordUpdateEvent extends UserEvent {

	private static final long serialVersionUID = -8804423693143313097L;
	
	public static final String EVENT_RESOURCE_KEY = "event.passwordUpdate";
	
	public PasswordUpdateEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<? extends Principal> associatedPrincipals, Map<String,String> properties) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal,
				associatedPrincipals, properties);
	}

	public PasswordUpdateEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties, List<? extends Principal> associatedPrincipals) {
		super(source, EVENT_RESOURCE_KEY, e, session, realm, provider,
				principalName, properties, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
