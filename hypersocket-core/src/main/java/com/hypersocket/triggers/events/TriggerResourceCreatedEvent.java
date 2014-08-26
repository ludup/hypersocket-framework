package com.hypersocket.triggers.events;

import com.hypersocket.session.Session;
import com.hypersocket.triggers.TriggerResource;

public class TriggerResourceCreatedEvent extends
		TriggerResourceEvent {

	private static final long serialVersionUID = 6695605859555262955L;

	public static final String EVENT_RESOURCE_KEY = "trigger.created";
	
	public TriggerResourceCreatedEvent(Object source,
			Session session,
			TriggerResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public TriggerResourceCreatedEvent(Object source,
			TriggerResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
