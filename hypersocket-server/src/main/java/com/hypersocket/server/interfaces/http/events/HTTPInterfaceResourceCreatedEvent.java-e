package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceCreatedEvent extends
		HTTPInterfaceResourceEvent {

	private static final long serialVersionUID = 5330261309395173523L;

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
