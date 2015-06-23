package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.session.Session;

public class UserAttributeCategoryDeletedEvent extends UserAttributeCategoryEvent {

	private static final long serialVersionUID = 6089611302300354505L;

	public static final String EVENT_RESOURCE_KEY = "event.categoryDeleted";

	public UserAttributeCategoryDeletedEvent(Object source, Session session,
			UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
	}

	public UserAttributeCategoryDeletedEvent(Object source, Throwable e,
			Session session, UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
