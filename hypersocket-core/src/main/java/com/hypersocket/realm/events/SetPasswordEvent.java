package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.ChargeableEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class SetPasswordEvent extends UserEvent implements ChargeableEvent {

	private static final long serialVersionUID = -7054465917307746647L;

	public static final String EVENT_RESOURCE_KEY = "event.setPassword";
	
	public static final String ATTR_ADMINISTRATIVE = "attr.administrative";
	
	public SetPasswordEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal, String password, boolean administrative) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal);
		addAttribute(ATTR_ADMINISTRATIVE, String.valueOf(administrative));
	}

	public SetPasswordEvent(Object source, Throwable t, Session session,
			Realm realm, RealmProvider provider, String principal, String password, boolean administrative) {
		super(source, EVENT_RESOURCE_KEY, t, session, realm, provider,
				principal);
		addAttribute(ATTR_ADMINISTRATIVE, String.valueOf(administrative));
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	@Override
	public Double getCharge() {
		return 5D;
	}
}
