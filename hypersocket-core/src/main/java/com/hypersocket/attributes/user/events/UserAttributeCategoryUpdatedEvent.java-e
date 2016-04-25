package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.session.Session;

public class UserAttributeCategoryUpdatedEvent extends UserAttributeCategoryEvent {

	private static final long serialVersionUID = -2152094139303539214L;

	public static final String ATTR_OLD_CATEGORY_NAME = "attr.oldCategoryName";

	public static final String EVENT_RESOURCE_KEY = "event.categoryUpdated";

	public UserAttributeCategoryUpdatedEvent(Object source, Session session, UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
	}

	public UserAttributeCategoryUpdatedEvent(Object source, Throwable e,
			Session session, UserAttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
