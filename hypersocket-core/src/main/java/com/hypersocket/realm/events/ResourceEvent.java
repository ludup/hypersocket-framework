package com.hypersocket.realm.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;
import com.hypersocket.utils.HypersocketUtils;

public class ResourceEvent extends SessionEvent {

	public static final String EVENT_RESOURCE_KEY = "resource.event";
	
	private static final long serialVersionUID = 1767418948626283958L;
	RealmResource resource;
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	public static final String ATTR_CREATED = "attr.created";
	public static final String ATTR_LAST_MODIFIED = "attr.lastModified";
	
	public ResourceEvent(Object source, String resourceKey, boolean success,
			Session session, RealmResource resource) {
		super(source, resourceKey, success, session);
		this.resource = resource;
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		addAttribute(ATTR_CREATED, HypersocketUtils.formatDate(resource.getCreateDate()));
		addAttribute(ATTR_LAST_MODIFIED, HypersocketUtils.formatDate(resource.getModifiedDate()));
	}

	public ResourceEvent(Object source, String resourceKey, Throwable e,
			Session session, RealmResource resource) {
		super(source, resourceKey, e, session);
		this.resource = resource;
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		addAttribute(ATTR_CREATED, HypersocketUtils.formatDate(resource.getCreateDate()));
		addAttribute(ATTR_LAST_MODIFIED, HypersocketUtils.formatDate(resource.getModifiedDate()));
	}


	public RealmResource getResource() {
		return resource;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
