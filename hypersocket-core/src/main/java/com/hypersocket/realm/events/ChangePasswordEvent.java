package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class ChangePasswordEvent extends UserEvent {


	private static final long serialVersionUID = -3803820258071069690L;

	public static final String EVENT_RESOURCE_KEY = "event.changePassword";

	public static final String ATTR_PASSWORD = UserCreatedEvent.ATTR_PASSWORD;
	
	public ChangePasswordEvent(Object source, Session session, Realm realm,
			RealmProvider provider, String password) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, session.getCurrentPrincipal());
		addAttribute(ATTR_PASSWORD, password);
	}

	public ChangePasswordEvent(Object source, Throwable t, Session session,
			Realm realm, RealmProvider provider, String password) {
		super(source, EVENT_RESOURCE_KEY, t, session, realm, provider,
				session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_PASSWORD, password);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
