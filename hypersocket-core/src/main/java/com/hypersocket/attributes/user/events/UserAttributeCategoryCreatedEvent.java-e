package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.session.Session;

public class UserAttributeCategoryCreatedEvent extends UserAttributeCategoryEvent {

	private static final long serialVersionUID = -5994713799720261311L;

	public static final String EVENT_RESOURCE_KEY = "event.categoryCreated";

	public UserAttributeCategoryCreatedEvent(Object source, Session session,
			UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
	}

	public UserAttributeCategoryCreatedEvent(Object source, Throwable e,
			Session session, UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
