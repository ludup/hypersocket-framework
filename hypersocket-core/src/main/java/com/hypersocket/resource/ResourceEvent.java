package com.hypersocket.resource;

import java.util.Map;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public abstract class ResourceEvent extends SessionEvent {

	private static final long serialVersionUID = -282443990103353325L;

	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	public ResourceEvent(Object source, String resourceKey, boolean success,
			Session session, Resource resource) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	public ResourceEvent(Object source, String resourceKey, Resource resource, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	
}
