package com.hypersocket.resource;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public abstract class ResourceSessionEvent extends SessionEvent {

	private static final long serialVersionUID = 5416406167471742408L;
	
	public static final String EVENT_RESOURCE_KEY = "resourceSession.event";
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	protected Resource resource;
	
	public ResourceSessionEvent(Object source, String resourceKey, boolean success,
			Session session, Resource resource) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		this.resource = resource;
	}
	
	public ResourceSessionEvent(Object source, String resourceKey, boolean success,
			Session session, Resource resource, Realm currentRealm) {
		super(source, resourceKey, success, session, currentRealm);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		this.resource = resource;
	}

	public ResourceSessionEvent(Object source, String resourceKey, String resourceName, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_RESOURCE_NAME, resourceName);
	}
	
	public ResourceSessionEvent(Object source, String resourceKey, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public abstract boolean isUsage();

	public String getResourceName() {
		return getAttribute(ATTR_RESOURCE_NAME);
	}
	
}
