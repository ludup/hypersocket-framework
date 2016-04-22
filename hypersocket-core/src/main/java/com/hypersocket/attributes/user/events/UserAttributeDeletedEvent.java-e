package com.hypersocket.attributes.user.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.session.Session;

public class UserAttributeDeletedEvent extends UserAttributeEvent {

	private static final long serialVersionUID = 5911872553198849151L;

	public static final String EVENT_RESOURCE_KEY = "event.userAttributeDeleted";

	public UserAttributeDeletedEvent(Object source, Session session,
			UserAttribute attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public UserAttributeDeletedEvent(Object source, Throwable e, Session session,
			UserAttribute attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
