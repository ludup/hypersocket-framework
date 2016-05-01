package com.hypersocket.attributes.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.RealmAttributeCategory;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class AttributeCategoryEvent extends ResourceEvent {

	private static final long serialVersionUID = 2403614014077914053L;

	public static final String EVENT_RESOURCE_KEY = "attributeCategory.event";
	public static final String ATTR_CATEGORY_NAME = "attr.categoryName";

	public AttributeCategoryEvent(Object source, String resourceKey,
			boolean success, Session session, RealmAttributeCategory category) {
		super(source, resourceKey, success, session, category);
		addAttribute(ATTR_CATEGORY_NAME, category.getName());
	}

	public AttributeCategoryEvent(Object source, String resourceKey,
			Throwable e, Session session, RealmAttributeCategory category) {
		super(source, resourceKey, e, session, category);
		addAttribute(ATTR_CATEGORY_NAME, category.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
