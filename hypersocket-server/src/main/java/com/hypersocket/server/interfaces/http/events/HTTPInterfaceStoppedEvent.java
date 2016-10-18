package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.events.InterfaceStoppedEvent;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceStoppedEvent extends
		InterfaceStoppedEvent {

	private static final long serialVersionUID = 1848652175561376124L;

	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.stopped";
	
	public HTTPInterfaceStoppedEvent(Object source,
			Session session,
			HTTPInterfaceResource resource,
			String boundInterface,
			int port) {
		super(source, EVENT_RESOURCE_KEY, session, resource, boundInterface, port);
	}

	public HTTPInterfaceStoppedEvent(Object source,
			HTTPInterfaceResource resource, Throwable e,
			Session session,
			String boundInterface,
			int port) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session, boundInterface, port);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
