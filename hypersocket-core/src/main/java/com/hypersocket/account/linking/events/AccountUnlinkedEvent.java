package com.hypersocket.account.linking.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class AccountUnlinkedEvent extends AccountLinkageEvent {

	private static final long serialVersionUID = -1433495331929236969L;

	public static final String EVENT_RESOURCE_KEY = "accountUnlinked.event";
	
	public AccountUnlinkedEvent(Object source, Session session, Realm realm, RealmProvider provider,
			Principal principal, Principal linkedPrincipal) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal, linkedPrincipal);
	}
	
	public AccountUnlinkedEvent(Object source, Session session, Realm realm, RealmProvider provider,
			Principal principal, Principal linkedPrincipal, Throwable t) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal, linkedPrincipal, t);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
