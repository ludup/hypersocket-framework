package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class UserAttributeEvent extends ResourceEvent {

	private static final long serialVersionUID = -1438866177845416396L;
	
	public static final String EVENT_RESOURCE_KEY = "userAttribute.event";
	public static final String ATTR_ATTRIBUTE_NAME = "attr.attributeName";

	public UserAttributeEvent(Object source, String resourceKey, boolean success,
			Session session, UserAttribute attribute) {
		super(source, resourceKey, success, session, attribute);
		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public UserAttributeEvent(Object source, String resourceKey, Throwable e,
			Session session, UserAttribute attribute) {
		super(source, resourceKey, e, session, attribute);
		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
