package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.events.InterfaceStartedEvent;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceStartedEvent extends
		InterfaceStartedEvent {

	private static final long serialVersionUID = 1848652175561376124L;

	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.started";
	
	public HTTPInterfaceStartedEvent(Object source,
			Session session,
			HTTPInterfaceResource resource, 
			String boundInterface) {
		super(source, EVENT_RESOURCE_KEY, session, resource, boundInterface);
	}

	public HTTPInterfaceStartedEvent(Object source,
			HTTPInterfaceResource resource, Throwable e,
			Session session,
			String boundInterface) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session, boundInterface);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
