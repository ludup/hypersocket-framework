package com.hypersocket.realm.events;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.local.LocalRealmProviderImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserCreatedEvent extends UserEvent {

	private static final long serialVersionUID = 128120714278922129L;

	public static final String EVENT_RESOURCE_KEY = "event.userCreated";

	public static final String ATTR_SELF_CREATED = "attr.selfCreated";
	public static final String ATTR_LOCAL_ACCOUNT = "attr.local";
	public static final String ATTR_PASSWORD = "attr.password";
	public static final String ATTR_PASSWORD_CHANGE_REQUIRED = "attr.passwordChangeRequired";
	
	{
		consoleLog = false;
	}

	public UserCreatedEvent(Object source, Session session, Realm realm, RealmProvider provider, Principal principal,
			List<? extends Principal> associatedPrincipals, Map<String, String> properties) {
		super(source, "event.userCreated", session, realm, provider, principal, associatedPrincipals, properties);
		addAttribute(ATTR_SELF_CREATED, String.valueOf(false));
		addAttribute(ATTR_LOCAL_ACCOUNT,
				String.valueOf(provider.getModule().equals(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY)));
	}

	public UserCreatedEvent(Object source, Session session, Realm realm, RealmProvider provider, Principal principal,
			List<? extends Principal> associatedPrincipals, Map<String, String> properties, String password,
			boolean forceChange, boolean selfCreated) {
		super(source, "event.userCreated", session, realm, provider, principal, associatedPrincipals, properties);
		addAttribute(ATTR_PASSWORD, password.substring(0, 2) + "**********");
		addAttribute(ATTR_PASSWORD_CHANGE_REQUIRED, String.valueOf(forceChange));
		addAttribute(ATTR_SELF_CREATED, String.valueOf(selfCreated));
		addAttribute(ATTR_LOCAL_ACCOUNT,
				String.valueOf(provider.getModule().equals(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY)));
	}

	public UserCreatedEvent(Object source, Throwable e, Session session, Realm realm, RealmProvider provider,
			String principalName, Map<String, String> properties, List<Principal> associatedPrincipals) {
		super(source, "event.userCreated", e, session, realm, provider, principalName, properties,
				associatedPrincipals);
		addAttribute(ATTR_LOCAL_ACCOUNT,
				String.valueOf(provider.getModule().equals(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY)));
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
