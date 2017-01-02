package com.hypersocket.attributes.role.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class RoleAttributeEvent extends ResourceEvent {

	private static final long serialVersionUID = 1821993365977806811L;

	public static final String EVENT_RESOURCE_KEY = "roleAttribute.event";
	public static final String ATTR_ATTRIBUTE_NAME = "attr.attributeName";

	public RoleAttributeEvent(Object source, String resourceKey, boolean success,
			Session session, AbstractAttribute<?> attribute) {
		super(source, resourceKey, success, session, attribute);
		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public RoleAttributeEvent(Object source, String resourceKey, Throwable e,
			Session session, AbstractAttribute<?> attribute) {
		super(source, resourceKey, e, session, attribute);
		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
