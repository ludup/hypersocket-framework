package com.hypersocket.realm.events;

import java.util.List;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public abstract class GroupEvent extends RealmEvent {

	private static final long serialVersionUID = 7696093164958120790L;

	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_ASSOCIATED_PRINCIPALS = "attr.associatedPrincipals";
	
	private Principal principal;

	public GroupEvent(Object source, String resourceKey, Session session, Realm realm, RealmProvider provider, Principal principal) {
		super(source, resourceKey, true, session, realm);
		this.principal = principal;
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getName());
	}
	
	public GroupEvent(Object source, String resourceKey, Session session, Realm realm, RealmProvider provider, Principal principal, List<Principal> associatedPrincipals) {
		super(source, resourceKey, true, session, realm);
		this.principal = principal;
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getName());
		addAssociatedPrincipals(associatedPrincipals);
		for(DatabaseProperty prop : principal.getPropertiesMap().values()) {
			addAttribute(prop.getResourceKey(), prop.getValue());
		}
	}

	public GroupEvent(Object source, String resourceKey, Throwable e,
			Session session, String realmName, RealmProvider provider, String principalName) {
		super(source, resourceKey, e, session, realmName);
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getName());
	}
	
	public GroupEvent(Object source, String resourceKey, Throwable e,
			Session session, String realmName, RealmProvider provider, String principalName, List<Principal> associatedPrincipals) {
		super(source, resourceKey, e, session, realmName);
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getName());
		addAssociatedPrincipals(associatedPrincipals);
	}
	
	private void addAssociatedPrincipals(List<Principal> associatedPrincipals) {
		StringBuffer buf = new StringBuffer();
		for(Principal p : associatedPrincipals) {
			if(buf.length() > 0) {
				buf.append(',');
			}
			buf.append(p.getPrincipalName());
		}
		addAttribute(ATTR_ASSOCIATED_PRINCIPALS, buf.toString());
	}

	public Principal getPrincipal() {
		return principal;
	}
	
}
