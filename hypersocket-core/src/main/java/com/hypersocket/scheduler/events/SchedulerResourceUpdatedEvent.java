package com.hypersocket.scheduler.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.session.Session;

public class SchedulerResourceUpdatedEvent extends SchedulerResourceEvent {

	private static final long serialVersionUID = -784245216689480508L;
	public static final String EVENT_RESOURCE_KEY = "scheduler.updated";

	public SchedulerResourceUpdatedEvent(Object source, Session session,
			SchedulerResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public SchedulerResourceUpdatedEvent(Object source,
			SchedulerResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
