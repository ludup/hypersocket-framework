package com.hypersocket.resource;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public abstract class ResourceEvent extends SessionEvent {

	private static final long serialVersionUID = -282443990103353325L;

	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	public ResourceEvent(Object source, String resourceKey, boolean success,
			Session session, Resource resource) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		for(DatabaseProperty prop : resource.getProperties().values()) {
			addAttribute(prop.getResourceKey(), prop.getValue());
		}
	}

	public ResourceEvent(Object source, String resourceKey, Resource resource, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	
}
