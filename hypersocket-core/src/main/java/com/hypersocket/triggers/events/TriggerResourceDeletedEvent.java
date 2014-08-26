package com.hypersocket.triggers.events;

import com.hypersocket.session.Session;
import com.hypersocket.triggers.TriggerResource;

public class TriggerResourceDeletedEvent extends
		TriggerResourceEvent {

	private static final long serialVersionUID = -1201717991458920787L;

	public static final String EVENT_RESOURCE_KEY = "trigger.deleted";

	public TriggerResourceDeletedEvent(Object source,
			Session session, TriggerResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public TriggerResourceDeletedEvent(Object source,
			TriggerResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
