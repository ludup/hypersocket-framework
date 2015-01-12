package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ResourceEvent extends SessionEvent {

	public static final String EVENT_RESOURCE_KEY = "resource.event";
	
	private static final long serialVersionUID = 1767418948626283958L;
	RealmResource resource;
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	
	public ResourceEvent(Object source, String resourceKey, boolean success,
			Session session, RealmResource resource) {
		super(source, resourceKey, success, session);
		this.resource = resource;
		if(resource.getRealm()!=null) {
			addAttribute(ATTR_REALM_NAME, resource.getRealm().getName());
		}
	}

	public ResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, RealmResource resource) {
		super(source, resourceKey, e, session);
		this.resource = resource;
		if(resource.getRealm()!=null) {
			addAttribute(ATTR_REALM_NAME, resource.getRealm().getName());
		}
	}
	
	public ResourceEvent(Object source, String resourceKey, boolean success,
			Session session, String realmName) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_REALM_NAME, realmName);
	}
	
	public ResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, String realmName) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_REALM_NAME, realmName);
	}

	public RealmResource getResource() {
		return resource;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
