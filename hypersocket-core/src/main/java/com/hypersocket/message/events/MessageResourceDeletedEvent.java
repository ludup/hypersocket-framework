package com.hypersocket.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.message.MessageResource;
import com.hypersocket.session.Session;

public class MessageResourceDeletedEvent extends
		MessageResourceEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2761984448107423821L;
	/**
	 * TODO rename to suit your resource and replace message with lower case
	 * name of your resource.
	 * 
	 * You typically add attributes to the base MessageResourceEvent
	 * class so these can be reused across all resource events.
	 */
	public static final String EVENT_RESOURCE_KEY = "message.deleted";

	public MessageResourceDeletedEvent(Object source,
			Session session, MessageResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public MessageResourceDeletedEvent(Object source,
			MessageResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
