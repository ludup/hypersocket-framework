package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;

public class PrincipalResumedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.principalResumed";

	public static final String ATTR_SUSPENDED_PRINCIPAL = "attr.resumedPrincipal";
	public static final String ATTR_SUSPENDED_PRINCIPAL_NAME = "attr.resumedPrincipalName";
	public static final String ATTR_SUSPENDED_PRINCIPAL_REALM_NAME = "attr.resumedPrincipalRealmName";
	public static final String ATTR_SUSPENDED_PRINCIPAL_REALM = "attr.resumedPrincipalRealm";
	public static final String ATTR_SUSPENDED_PRINCIPAL_REALM_TYPE = "attr.resumedPrincipalRealmType";

	private Principal resumedPrincipal;
	
	public PrincipalResumedEvent(Object source, Session session,
			Principal principal, Principal enabledAccount) {
		super(source, EVENT_RESOURCE_KEY, session, principal.getRealm(), null, principal);
		this.resumedPrincipal = enabledAccount;
		addAttributes(enabledAccount);
	}

	public PrincipalResumedEvent(Object source, Throwable e, Session session,
			Principal principal, Principal enabledAccount) {
		super(source, EVENT_RESOURCE_KEY, e, session, principal.getRealm(), null,
				principal.getName());
		this.resumedPrincipal = enabledAccount;
		addAttributes(enabledAccount);
	}
	
	public Principal getResumedPrincipal() {
		return resumedPrincipal;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private void addAttributes(Principal enabledAccount) {
		addAttribute(ATTR_SUSPENDED_PRINCIPAL, enabledAccount.getId());
		addAttribute(ATTR_SUSPENDED_PRINCIPAL_NAME, enabledAccount.getName());
		addAttribute(ATTR_SUSPENDED_PRINCIPAL_REALM, enabledAccount.getRealm().getId());
		addAttribute(ATTR_SUSPENDED_PRINCIPAL_REALM_NAME, enabledAccount.getRealmName());
		addAttribute(ATTR_SUSPENDED_PRINCIPAL_REALM_TYPE, enabledAccount.getRealmModule());
	}
}
