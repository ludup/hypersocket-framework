package com.hypersocket.realm.events;

import java.util.List;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class GroupCreatedEvent extends GroupEvent {

	private static final long serialVersionUID = 4735780813485949894L;

	public GroupCreatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<Principal> associatedPrincipals) {
		super(source, "event.groupCreated", session, realm, provider, principal,
				associatedPrincipals);
	}

	public GroupCreatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			List<Principal> associatedPrincipals) {
		super(source, "event.groupCreated", e, session, realm.getName(),
				provider, principalName, associatedPrincipals);
	}

}
