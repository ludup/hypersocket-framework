package com.hypersocket.automation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.session.Session;

public class AutomationResourceUpdatedEvent extends
		AutomationResourceEvent {

	private static final long serialVersionUID = -8825515471879941765L;

	public static final String EVENT_RESOURCE_KEY = "automation.updated";

	public AutomationResourceUpdatedEvent(Object source,
			Session session, AutomationResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public AutomationResourceUpdatedEvent(Object source,
			AutomationResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
