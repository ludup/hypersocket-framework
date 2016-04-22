package com.hypersocket.scheduler.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.session.Session;

public class SchedulerResourceDeletedEvent extends SchedulerResourceEvent {

	private static final long serialVersionUID = -7069066344569181990L;
	public static final String EVENT_RESOURCE_KEY = "scheduler.deleted";

	public SchedulerResourceDeletedEvent(Object source, Session session,
			SchedulerResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public SchedulerResourceDeletedEvent(Object source,
			SchedulerResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
