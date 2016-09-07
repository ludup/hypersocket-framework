package com.hypersocket.account.linking.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class AccountLinkedEvent extends AccountLinkageEvent {

	public static final String EVENT_RESOURCE_KEY = "accountLinked.event";
	
	private static final long serialVersionUID = 8698616023543634131L;

	public AccountLinkedEvent(Object source, Session session, Realm realm, RealmProvider provider,
			Principal principal, Principal linkedPrincipal) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal, linkedPrincipal);
	}
	
	public AccountLinkedEvent(Object source, Session session, Realm realm, RealmProvider provider,
			Principal principal, Principal linkedPrincipal, Throwable t) {
		super(source, EVENT_RESOURCE_KEY, session, realm, provider, principal, linkedPrincipal, t);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
