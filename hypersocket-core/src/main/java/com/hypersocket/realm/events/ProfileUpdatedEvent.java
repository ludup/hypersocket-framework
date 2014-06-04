package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class ProfileUpdatedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public ProfileUpdatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<Principal> associatedPrincipals) {
		super(source, "event.profileUpdated", session, realm, provider, principal,
				associatedPrincipals);
	}

	public ProfileUpdatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties, List<Principal> associatedPrincipals) {
		super(source, "event.profileUpdated", e, session, realm.getName(), provider,
				principalName, properties, associatedPrincipals);
	}

}
