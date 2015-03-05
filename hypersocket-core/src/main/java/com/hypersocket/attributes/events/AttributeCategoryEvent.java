package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class AttributeCategoryEvent extends SessionEvent {

	private static final long serialVersionUID = 2403614014077914053L;

	public static final String EVENT_RESOURCE_KEY = "attributeCategory.event";
	public static final String ATTR_CATEGORY_NAME = "attr.categoryName";

	public AttributeCategoryEvent(Object source, String resourceKey,
			boolean success, Session session, AttributeCategory category) {
		super(source, resourceKey, success, session);

		addAttribute(ATTR_CATEGORY_NAME, category.getName());
	}

	public AttributeCategoryEvent(Object source, String resourceKey,
			Throwable e, Session session, AttributeCategory category) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_CATEGORY_NAME, category.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
