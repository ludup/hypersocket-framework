package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserImpersonatedEvent extends UserEvent {

	private static final long serialVersionUID = 128120714278922129L;

	public static final String EVENT_RESOURCE_KEY = "event.userImpersonated";

	{
		consoleLog = false;
	}

	public UserImpersonatedEvent(Object source, Session session, Realm realm, 
			RealmProvider provider,
			Principal principal,
			String impersonatedUserName) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
