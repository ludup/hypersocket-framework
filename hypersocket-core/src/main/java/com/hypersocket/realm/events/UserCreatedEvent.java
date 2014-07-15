package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserCreatedEvent extends UserEvent {

	private static final long serialVersionUID = 128120714278922129L;

	public UserCreatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<Principal> associatedPrincipals, Map<String,String> properties) {
		super(source, "event.userCreated", session, realm, provider, principal,
				associatedPrincipals, properties);
	}

	public UserCreatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties, List<Principal> associatedPrincipals) {
		super(source, "event.userCreated", e, session, realm.getName(), provider,
				principalName, properties, associatedPrincipals);
	}

}
