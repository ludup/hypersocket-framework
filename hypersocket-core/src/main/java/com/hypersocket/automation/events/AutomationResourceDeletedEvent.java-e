package com.hypersocket.automation.events;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.session.Session;

public class AutomationResourceDeletedEvent extends
		AutomationResourceEvent {

	private static final long serialVersionUID = -2888551456987123724L;

	public static final String EVENT_RESOURCE_KEY = "automation.deleted";

	public AutomationResourceDeletedEvent(Object source,
			Session session, AutomationResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public AutomationResourceDeletedEvent(Object source,
			AutomationResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
