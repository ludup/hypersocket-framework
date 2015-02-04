package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.session.Session;

public class AttributeDeletedEvent extends AttributeEvent {

	private static final long serialVersionUID = 5911872553198849151L;

	public static final String EVENT_RESOURCE_KEY = "event.attributeDeleted";

	public AttributeDeletedEvent(Object source, Session session,
			Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public AttributeDeletedEvent(Object source, Throwable e, Session session,
			Attribute attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
