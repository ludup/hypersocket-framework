package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class SetPasswordEvent extends UserEvent {

	private static final long serialVersionUID = -7054465917307746647L;

	public static final String EVENT_RESOURCE_KEY = "event.setPassword";

	public SetPasswordEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal);
	}

	public SetPasswordEvent(Object source, Throwable t, Session session,
			Realm realm, RealmProvider provider, String principal) {
		super(source, EVENT_RESOURCE_KEY, t, session, realm, provider,
				principal);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
