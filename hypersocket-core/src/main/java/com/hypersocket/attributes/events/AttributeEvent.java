package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class AttributeEvent extends SessionEvent {

	private static final long serialVersionUID = -1438866177845416396L;
	
	public static final String EVENT_RESOURCE_KEY = "attribute.event";
	public static final String ATTR_ATTRIBUTE_NAME = "attr.attributeName";

	public AttributeEvent(Object source, String resourceKey, boolean success,
			Session session, Attribute attribute) {
		super(source, resourceKey, success, session);

		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public AttributeEvent(Object source, String resourceKey, Throwable e,
			Session session, Attribute attribute) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_ATTRIBUTE_NAME, attribute.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
