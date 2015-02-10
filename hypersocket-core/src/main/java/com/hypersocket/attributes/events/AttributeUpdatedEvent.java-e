package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.session.Session;

public class AttributeUpdatedEvent extends AttributeEvent {

	private static final long serialVersionUID = -3669089170984706952L;

	public static final String ATTR_OLD_ATTRIBUTE_NAME = "attr.oldAttributemName";

	public static final String EVENT_RESOURCE_KEY = "event.attributeUpdated";

	public AttributeUpdatedEvent(Object source, Session session,
			String oldName, Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
		addAttribute(ATTR_OLD_ATTRIBUTE_NAME, oldName);
	}

	public AttributeUpdatedEvent(Object source, Throwable e, Session session,
			Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
