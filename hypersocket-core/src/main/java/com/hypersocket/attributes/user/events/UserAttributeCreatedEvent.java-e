package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.session.Session;

public class AttributeCreatedEvent extends AttributeEvent {

	private static final long serialVersionUID = -7174259161714227120L;

	public static final String EVENT_RESOURCE_KEY = "event.attributeCreated";

	public AttributeCreatedEvent(Object source, Session session,
			Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public AttributeCreatedEvent(Object source, Throwable e, Session session,
			Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
