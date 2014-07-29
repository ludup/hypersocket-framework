package com.hypersocket.realm.events;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class RealmResourceEvent extends SessionEvent {

	private static final long serialVersionUID = 1767418948626283958L;
	RealmResource resource;
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	
	public RealmResourceEvent(Object source, String resourceKey, boolean success,
			Session session, RealmResource resource) {
		super(source, resourceKey, success, session);
		this.resource = resource;
		if(resource.getRealm()!=null) {
			addAttribute(ATTR_REALM_NAME, resource.getRealm().getName());
		}
	}

	public RealmResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, RealmResource resource) {
		super(source, resourceKey, e, session);
		this.resource = resource;
		if(resource.getRealm()!=null) {
			addAttribute(ATTR_REALM_NAME, resource.getRealm().getName());
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

	public RealmResource getResource() {
		return resource;
	}

}
