package com.hypersocket.jobs.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.jobs.JobResource;
import com.hypersocket.session.Session;

public class JobResourceUpdatedEvent extends
		JobResourceEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6513139602203267668L;
	/**
	 * TODO rename to suit your resource and replace job with lower case
	 * name of your resource.
	 * 
	 * You typically add attributes to the base JobResourceEvent
	 * class so these can be reused across all resource events.
	 */
	public static final String EVENT_RESOURCE_KEY = "job.updated";

	public JobResourceUpdatedEvent(Object source,
			Session session, JobResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public JobResourceUpdatedEvent(Object source,
			JobResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
