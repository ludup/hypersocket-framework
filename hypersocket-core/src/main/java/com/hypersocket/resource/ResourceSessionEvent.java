package com.hypersocket.resource;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ResourceSessionEvent extends SessionEvent {

	private static final long serialVersionUID = 5416406167471742408L;
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	public ResourceSessionEvent(Object source, String resourceKey, boolean success,
			Session session, Resource resource) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	public ResourceSessionEvent(Object source, String resourceKey, String resourceName, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_RESOURCE_NAME, resourceName);
	}

	
}
