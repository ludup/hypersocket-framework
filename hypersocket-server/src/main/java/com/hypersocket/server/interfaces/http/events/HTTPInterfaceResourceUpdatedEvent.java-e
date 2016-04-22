package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceUpdatedEvent extends
		HTTPInterfaceResourceEvent {

	private static final long serialVersionUID = -8007834648855709309L;

	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.updated";

	public HTTPInterfaceResourceUpdatedEvent(Object source,
			Session session, HTTPInterfaceResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public HTTPInterfaceResourceUpdatedEvent(Object source,
			HTTPInterfaceResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
