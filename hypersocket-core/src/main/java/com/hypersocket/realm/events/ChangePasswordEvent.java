package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class ChangePasswordEvent extends UserEvent {


	private static final long serialVersionUID = -3803820258071069690L;

	public static final String EVENT_RESOURCE_KEY = "event.changePassword";
	
	public ChangePasswordEvent(Object source, Session session, Realm realm,
			RealmProvider provider, String password, Principal principal) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal);
	}

	public ChangePasswordEvent(Object source, Throwable t, Session session,
			Realm realm, RealmProvider provider, String password, Principal principal) {
		super(source, EVENT_RESOURCE_KEY, t, session, realm, provider,
				principal.getPrincipalName());
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
