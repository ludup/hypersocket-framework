package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class AccountDisabledEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.accountDisabled";

	public static final String ATTR_DISABLED_PRINCIPAL = "attr.disabledPrincipal";
	public static final String ATTR_DISABLED_PRINCIPAL_NAME = "attr.disabledPrincipalName";
	public static final String ATTR_DISABLED_PRINCIPAL_REALM_NAME = "attr.disabledRealmName";
	public static final String ATTR_DISABLED_PRINCIPAL_REALM = "attr.disabledRealm";
	public static final String ATTR_DISABLED_PRINCIPAL_REALM_TYPE = "attr.disabledRealmType";
	
	private Principal disabledAccount;

	public AccountDisabledEvent(Object source, Session session, RealmProvider provider,
			Principal principal, Principal disabledAccount) {
		super(source, EVENT_RESOURCE_KEY, session, principal.getRealm(), provider, principal);
		this.disabledAccount = disabledAccount;
		addAttributes(disabledAccount);
	}

	public AccountDisabledEvent(Object source, Throwable e, Session session, RealmProvider provider,
			Principal principal, Principal disabledAccount) {
		super(source, EVENT_RESOURCE_KEY, e, session, principal.getRealm(), provider, principal.getName());
		this.disabledAccount = disabledAccount;
		addAttributes(disabledAccount);
	}
	
	public Principal getDisabledAccount() {
		return disabledAccount;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private void addAttributes(Principal disabledAccount) {
		addAttribute(ATTR_DISABLED_PRINCIPAL, disabledAccount.getId());
		addAttribute(ATTR_DISABLED_PRINCIPAL_NAME, disabledAccount.getName());
		addAttribute(ATTR_DISABLED_PRINCIPAL_REALM, disabledAccount.getRealm().getId());
		addAttribute(ATTR_DISABLED_PRINCIPAL_REALM_NAME, disabledAccount.getRealmName());
		addAttribute(ATTR_DISABLED_PRINCIPAL_REALM_TYPE, disabledAccount.getRealmModule());
	}
}
