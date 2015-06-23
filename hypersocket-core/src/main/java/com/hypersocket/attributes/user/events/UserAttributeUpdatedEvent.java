package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.session.Session;

public class UserAttributeUpdatedEvent extends UserAttributeEvent {

	private static final long serialVersionUID = -3669089170984706952L;

	public static final String ATTR_OLD_ATTRIBUTE_NAME = "attr.oldAttributemName";

	public static final String EVENT_RESOURCE_KEY = "event.userAttributeUpdated";

	public UserAttributeUpdatedEvent(Object source, Session session,
			UserAttribute attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public UserAttributeUpdatedEvent(Object source, Throwable e, Session session,
			UserAttribute attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
