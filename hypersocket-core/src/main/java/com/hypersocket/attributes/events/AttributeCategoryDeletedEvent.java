package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.session.Session;

public class AttributeCategoryDeletedEvent extends AttributeCategoryEvent {

	private static final long serialVersionUID = 6089611302300354505L;

	public static final String EVENT_RESOURCE_KEY = "event.categoryDeleted";

	public AttributeCategoryDeletedEvent(Object source, Session session,
			AttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
	}

	public AttributeCategoryDeletedEvent(Object source, Throwable e,
			Session session, AttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
