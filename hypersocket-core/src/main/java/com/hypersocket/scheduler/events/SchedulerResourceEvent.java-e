package com.hypersocket.scheduler.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.session.Session;

public class SchedulerResourceEvent extends AssignableResourceEvent {

	// public static final String ATTR_NAME = "attr.name";

	private static final long serialVersionUID = -5982908195483181087L;
	public static final String EVENT_RESOURCE_KEY = "scheduler.event";

	public SchedulerResourceEvent(Object source, String resourceKey,
			Session session, SchedulerResource resource) {
		super(source, resourceKey, true, session, resource);

		/**
		 * TODO add attributes of your resource here. Make sure all attributes
		 * have a constant string definition like the commented out example
		 * above, its important for its name to start with ATTR_ as this is
		 * picked up during the registration process
		 */
	}

	public SchedulerResourceEvent(Object source, String resourceKey,
			SchedulerResource resource, Throwable e, Session session) {
		super(source, resourceKey, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
