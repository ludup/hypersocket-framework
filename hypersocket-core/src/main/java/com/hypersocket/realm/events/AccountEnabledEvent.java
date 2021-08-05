package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class AccountEnabledEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.accountEnabled";

	public static final String ATTR_ENABLED_PRINCIPAL = "attr.enabledPrincipal";
	public static final String ATTR_ENABLED_PRINCIPAL_NAME = "attr.enabledPrincipalName";
	public static final String ATTR_ENABLED_PRINCIPAL_REALM_NAME = "attr.enabledRealmName";
	public static final String ATTR_ENABLED_PRINCIPAL_REALM = "attr.enabledRealm";
	public static final String ATTR_ENABLED_PRINCIPAL_REALM_TYPE = "attr.enabledRealmType";

	private Principal enabledAccount;
	
	public AccountEnabledEvent(Object source, Session session,
			RealmProvider provider, Principal principal, Principal enabledAccount) {
		super(source, EVENT_RESOURCE_KEY, session, principal.getRealm(), provider, principal);
		this.enabledAccount = enabledAccount;
		addAttributes(enabledAccount);
	}

	public AccountEnabledEvent(Object source, Throwable e, Session session,
			RealmProvider provider, Principal principal, Principal enabledAccount) {
		super(source, EVENT_RESOURCE_KEY, e, session, principal.getRealm(), provider,
				principal.getName());
		this.enabledAccount = enabledAccount;
		addAttributes(enabledAccount);
	}
	
	public Principal getDisabledAccount() {
		return enabledAccount;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private void addAttributes(Principal enabledAccount) {
		addAttribute(ATTR_ENABLED_PRINCIPAL, enabledAccount.getId());
		addAttribute(ATTR_ENABLED_PRINCIPAL_NAME, enabledAccount.getName());
		addAttribute(ATTR_ENABLED_PRINCIPAL_REALM, enabledAccount.getRealm().getId());
		addAttribute(ATTR_ENABLED_PRINCIPAL_REALM_NAME, enabledAccount.getRealmName());
		addAttribute(ATTR_ENABLED_PRINCIPAL_REALM_TYPE, enabledAccount.getRealmModule());
	}
}
