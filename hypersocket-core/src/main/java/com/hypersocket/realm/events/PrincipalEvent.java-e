package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class PrincipalEvent extends SessionEvent {

	private static final long serialVersionUID = 1767418948626283958L;
	Realm realm;
	
	public static final String EVENT_RESOURCE_KEY = "principal.event";
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	public static final String ATTR_REALM_TYPE = "attr.realmType";
	
	public PrincipalEvent(Object source, String resourceKey, boolean success,
			Session session, Realm realm) {
		super(source, resourceKey, success, session, realm);
		this.realm = realm;
		addAttribute(ATTR_REALM_NAME, realm.getName());
		addAttribute(ATTR_REALM_TYPE, realm.getResourceCategory());
	}

	public PrincipalEvent(Object source, String resourceKey, Throwable e,
			Session session, Realm realm) {
		super(source, resourceKey, e, session);
		this.realm = realm;
		addAttribute(ATTR_REALM_NAME, realm.getName());
		addAttribute(ATTR_REALM_TYPE, realm.getResourceCategory());
	}
	
	public PrincipalEvent(Object source, String resourceKey, boolean success,
			Session session, String realmName, String realmType) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_REALM_NAME, realmName);
		addAttribute(ATTR_REALM_TYPE, realmType);
	}
	
	public PrincipalEvent(Object source, String resourceKey, Throwable e,
			Session session, String realmName, String realmType) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_REALM_NAME, realmName);
		addAttribute(ATTR_REALM_TYPE, realmType);
	}

	public Realm getRealm() {
		return realm;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
