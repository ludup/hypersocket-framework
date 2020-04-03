package com.hypersocket.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.message.MessageResource;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class MessageResourceEvent extends ResourceEvent {

	private static final long serialVersionUID = 6693971807084150159L;
	public static final String EVENT_RESOURCE_KEY = "message.event";
	
	public MessageResourceEvent(Object source, String resourceKey,
			Session session, MessageResource resource) {
		super(source, resourceKey, true, session, resource);
	}

	public MessageResourceEvent(Object source, String resourceKey,
			MessageResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
