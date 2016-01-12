package com.hypersocket.server.interfaces.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceCreatedEvent extends
		HTTPInterfaceResourceEvent {

	/**
	 * TODO rename to suit your resource and replace hTTPInterface with lower case
	 * name of your resource.
	 * 
	 * You typically add attributes to the base HTTPInterfaceResourceEvent class
	 * so these can be reused across all resource events.
	 */
	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.created";
	
	public HTTPInterfaceResourceCreatedEvent(Object source,
			Session session,
			HTTPInterfaceResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public HTTPInterfaceResourceCreatedEvent(Object source,
			HTTPInterfaceResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
