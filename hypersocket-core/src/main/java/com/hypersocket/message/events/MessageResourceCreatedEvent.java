package com.hypersocket.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.message.MessageResource;
import com.hypersocket.session.Session;

public class MessageResourceCreatedEvent extends
		MessageResourceEvent {

	private static final long serialVersionUID = 1891466997434356621L;
	
	public static final String EVENT_RESOURCE_KEY = "message.created";
	
	public MessageResourceCreatedEvent(Object source,
			Session session,
			MessageResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public MessageResourceCreatedEvent(Object source,
			MessageResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
