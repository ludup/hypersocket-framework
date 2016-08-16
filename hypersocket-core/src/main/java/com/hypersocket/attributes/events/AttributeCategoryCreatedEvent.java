package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.RealmAttributeCategory;
import com.hypersocket.session.Session;

public class AttributeCategoryCreatedEvent extends AttributeCategoryEvent {

	private static final long serialVersionUID = -5994713799720261311L;

	public static final String EVENT_RESOURCE_KEY = "event.categoryCreated";

	public AttributeCategoryCreatedEvent(Object source, Session session,
			RealmAttributeCategory<?> category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
	}

	public AttributeCategoryCreatedEvent(Object source, Throwable e,
			Session session, RealmAttributeCategory<?> category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
