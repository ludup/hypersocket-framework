package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.session.Session;

public class AttributeCategoryUpdatedEvent extends AttributeCategoryEvent {

	private static final long serialVersionUID = -2152094139303539214L;

	public static final String ATTR_OLD_CATEGORY_NAME = "attr.oldCategoryName";

	public static final String EVENT_RESOURCE_KEY = "event.categoryUpdated";

	public AttributeCategoryUpdatedEvent(Object source, Session session,
			String oldName, AttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, true, session, category);
		addAttribute(ATTR_OLD_CATEGORY_NAME, oldName);
	}

	public AttributeCategoryUpdatedEvent(Object source, Throwable e,
			Session session, AttributeCategory category) {
		super(source, EVENT_RESOURCE_KEY, e, session, category);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
