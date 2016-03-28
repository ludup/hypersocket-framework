package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceDeletedEvent extends
		HTTPInterfaceResourceEvent {

	private static final long serialVersionUID = -6817358126984800977L;

	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.deleted";

	public HTTPInterfaceResourceDeletedEvent(Object source,
			Session session, HTTPInterfaceResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public HTTPInterfaceResourceDeletedEvent(Object source,
			HTTPInterfaceResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
