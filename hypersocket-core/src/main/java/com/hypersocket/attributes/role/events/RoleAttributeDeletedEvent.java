package com.hypersocket.attributes.role.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.session.Session;

public class RoleAttributeDeletedEvent extends RoleAttributeEvent {

	private static final long serialVersionUID = -2577816015097916631L;
	
	public static final String EVENT_RESOURCE_KEY = "event.roleAttributeDeleted";

	public RoleAttributeDeletedEvent(Object source, Session session, AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, true, session, attribute);
	}

	public RoleAttributeDeletedEvent(Object source, Throwable e, Session session, AbstractAttribute<?> attribute) {
		super(source, EVENT_RESOURCE_KEY, e, session, attribute);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
