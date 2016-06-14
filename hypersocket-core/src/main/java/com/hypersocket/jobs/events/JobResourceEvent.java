package com.hypersocket.jobs.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.jobs.JobResource;
import com.hypersocket.session.Session;

public class JobResourceEvent extends ResourceEvent {

	private static final long serialVersionUID = 921710635139797086L;

	public static final String EVENT_RESOURCE_KEY = "job.event";
	
	public JobResourceEvent(Object source, String resourceKey,
			Session session, JobResource resource) {
		super(source, resourceKey, true, session, resource);
	}

	public JobResourceEvent(Object source, String resourceKey,
			JobResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
