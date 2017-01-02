package com.hypersocket.attributes.role.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.session.Session;

public class RoleAttributeUpdatedEvent extends RoleAttributeEvent {

	private static final long serialVersionUID = -7818497927821127101L;

	public static final String ATTR_OLD_ATTRIBUTE_NAME = "attr.oldAttributemName";

	public static final String EVENT_RESOURCE_KEY = "event.roleAttributeUpdated";

	public RoleAttributeUpdatedEvent(Object source, Session session, AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public RoleAttributeUpdatedEvent(Object source, Throwable e, Session session, AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
