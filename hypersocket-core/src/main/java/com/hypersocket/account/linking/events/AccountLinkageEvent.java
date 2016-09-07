package com.hypersocket.account.linking.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.session.Session;

public class AccountLinkageEvent extends UserEvent {

	public static final String EVENT_RESOURCE_KEY = "accountLinkage.event";
	
	private static final long serialVersionUID = 9151869373598895201L;

	public static final String ATTR_LINKED_PRINCIPAL_NAME = "attr.linkedPrincipalName";
	public static final String ATTR_LINKED_PRINCIPAL_REALM = "attr.linkedPrincipalRealm";
	
	Principal linkedPrincipal;
	
	public AccountLinkageEvent(Object source, String resourceKey, Session session, Realm realm, RealmProvider provider,
			Principal principal, Principal linkedPrincipal) {
		super(source, resourceKey, session, realm, provider, principal);
		this.linkedPrincipal = linkedPrincipal;
		addAttribute(ATTR_LINKED_PRINCIPAL_NAME, linkedPrincipal.getName());
		addAttribute(ATTR_LINKED_PRINCIPAL_REALM, linkedPrincipal.getRealm().getName());
	}
	
	public AccountLinkageEvent(Object source, String resourceKey, Session session, Realm realmName,
			RealmProvider provider, Principal principal, Principal linkedPrincipal, Throwable e) {
		super(source, resourceKey, e, session, realmName, provider, principal.getPrincipalName());
		addAttribute(ATTR_LINKED_PRINCIPAL_NAME, linkedPrincipal.getName());
		addAttribute(ATTR_LINKED_PRINCIPAL_REALM, linkedPrincipal.getRealm().getName());
	}
	
	public Principal getLinkedPrincipal() {
		return linkedPrincipal;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
