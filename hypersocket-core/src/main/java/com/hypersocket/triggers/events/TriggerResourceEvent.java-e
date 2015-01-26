package com.hypersocket.triggers.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;
import com.hypersocket.triggers.TriggerResource;

public class TriggerResourceEvent extends ResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "trigger.event";
	
	private static final long serialVersionUID = 2854425020068386496L;

	public TriggerResourceEvent(Object source, String resourceKey,
			Session session, TriggerResource resource) {
		super(source, resourceKey, true, session, resource);
	}

	public TriggerResourceEvent(Object source, String resourceKey,
			TriggerResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
