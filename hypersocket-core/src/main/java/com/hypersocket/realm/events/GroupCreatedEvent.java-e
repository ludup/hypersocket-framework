package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class GroupCreatedEvent extends GroupEvent {

	private static final long serialVersionUID = 4735780813485949894L;

	public static final String EVENT_RESOURCE_KEY = "event.groupCreated";
	
	public GroupCreatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<Principal> associatedPrincipals,
			Map<String,String> properties) {
		super(source, "event.groupCreated", session, realm, provider, principal,
				associatedPrincipals, properties);
	}

	public GroupCreatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			List<Principal> associatedPrincipals) {
		super(source, "event.groupCreated", e, session, realm,
				provider, principalName, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
