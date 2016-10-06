package com.hypersocket.realm.events;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public abstract class GroupEvent extends PrincipalEvent {

	private static final long serialVersionUID = 7696093164958120790L;

	public static final String EVENT_RESOURCE_KEY = "group.event";

	public static final String ATTR_GROUP_NAME = "attr.group";
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_ASSOCIATED_PRINCIPALS = "attr.associatedPrincipals";

	private Principal principal;
	Collection<Principal> assosiatedPrincipals;
	
	public GroupEvent(Object source, String resourceKey, Session session,
			Realm realm, RealmProvider provider, Principal principal,
			Collection<Principal> associatedPrincipals) {
		super(source, resourceKey, true, session, realm);
		this.principal = principal;
		this.assosiatedPrincipals = associatedPrincipals;
		addAttribute(ATTR_GROUP_NAME, principal.getName());
		addAssociatedPrincipals(associatedPrincipals);
	}

	public GroupEvent(Object source, String resourceKey, Throwable e,
			Session session, Realm realmName, RealmProvider provider,
			String principalName, Collection<Principal> associatedPrincipals) {
		super(source, resourceKey, e, session, realmName);
		this.assosiatedPrincipals = associatedPrincipals;
		addAttribute(ATTR_GROUP_NAME, principalName);
		addAssociatedPrincipals(associatedPrincipals);
	}

	private void addAssociatedPrincipals(Collection<Principal> associatedPrincipals) {
		addAttribute(ATTR_ASSOCIATED_PRINCIPALS, createPrincipalList(associatedPrincipals));
	}

	public Principal getPrincipal() {
		return principal;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public Collection<Principal> getAssosicatedPrincipals() {
		return assosiatedPrincipals;
	}
	
	protected String createPrincipalList(Collection<Principal> principals) {
		StringBuffer buf = new StringBuffer();
		for(Principal p : principals) {
			if(buf.length() > 0) {
				buf.append("\r\n");
			}
			buf.append(p.getPrincipalName());
		}
		return buf.toString();
	}
}
