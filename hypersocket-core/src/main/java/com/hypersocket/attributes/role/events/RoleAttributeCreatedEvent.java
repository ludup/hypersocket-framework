package com.hypersocket.attributes.role.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.session.Session;

public class RoleAttributeCreatedEvent extends RoleAttributeEvent {

	private static final long serialVersionUID = -7259343792010609223L;

	public static final String EVENT_RESOURCE_KEY = "event.roleAttributeCreated";

	public RoleAttributeCreatedEvent(Object source, Session session,
			AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public RoleAttributeCreatedEvent(Object source, Throwable e, Session session,
			AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
