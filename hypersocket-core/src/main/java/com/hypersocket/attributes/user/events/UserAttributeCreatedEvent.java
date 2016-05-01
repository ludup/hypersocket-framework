package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.session.Session;

public class UserAttributeCreatedEvent extends UserAttributeEvent {

	private static final long serialVersionUID = -7174259161714227120L;

	public static final String EVENT_RESOURCE_KEY = "event.userAttributeCreated";

	public UserAttributeCreatedEvent(Object source, Session session,
			AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public UserAttributeCreatedEvent(Object source, Throwable e, Session session,
			AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
