package com.hypersocket.realm.events;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.ChargeableEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class ResetPasswordEvent extends UserEvent implements ChargeableEvent {

	private static final long serialVersionUID = -7054465917307746647L;

	public static final String EVENT_RESOURCE_KEY = "event.resetPassword";
	
	String password;
	
	public ResetPasswordEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal, String password) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal);
		this.password = password;
		Map<String,String> properties=  principal.getProperties();
		if(properties!=null) {
			for (String prop : properties.keySet()) {
				addAttribute(prop, properties.get(prop));
			}
		}
	}

	public ResetPasswordEvent(Object source, Throwable t, Session session,
			Realm realm, RealmProvider provider, String principal, String password) {
		super(source, EVENT_RESOURCE_KEY, t, session, realm, provider,
				principal);
		this.password = password;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	@Override
	public Double getCharge() {
		return 5D;
	}
	
	public String getPassword() {
		return password;
	}
}
