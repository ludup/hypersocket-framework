package com.hypersocket.realm.events;

import java.util.Map;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class RealmResourceEvent extends SessionEvent {

	private static final long serialVersionUID = 1767418948626283958L;
	Realm realm;
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	
	public RealmResourceEvent(Object source, String resourceKey, boolean success,
			Session session, Realm realm, Map<String,String> properties) {
		super(source, resourceKey, success, session);
		this.realm = realm;
		addAttributes(properties);
	}

	public RealmResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, Realm realm, Map<String,String> properties) {
		super(source, resourceKey, e, session);
		this.realm = realm;
		addAttributes(properties);
	}
	
	private void addAttributes(Map<String,String> properties) {
		addAttribute(ATTR_REALM_NAME, realm.getName());
		for(String prop : properties.keySet()) {
			addAttribute(prop, properties.get(prop));
		}
	}
	
	public RealmResourceEvent(Object source, String resourceKey, boolean success,
			Session session, String realmName) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_REALM_NAME, realmName);
	}
	
	public RealmResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, String realmName) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_REALM_NAME, realmName);
	}

	public Realm getRealm() {
		return realm;
	}

}
