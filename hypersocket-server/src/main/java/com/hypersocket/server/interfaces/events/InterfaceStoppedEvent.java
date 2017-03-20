package com.hypersocket.server.interfaces.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.DefaultEvent;
import com.hypersocket.server.interfaces.InterfaceResource;
import com.hypersocket.session.Session;

public class InterfaceStoppedEvent extends InterfaceResourceEvent implements DefaultEvent {
	
	private static final long serialVersionUID = 8606170445099375558L;
	
	public static final String EVENT_RESOURCE_KEY = "interfaceStarted.event";
	
	public InterfaceStoppedEvent(Object source, String resourceKey,
			Session session, InterfaceResource resource,
			String boundInterface, int port) {
		super(source, resourceKey, session, resource, boundInterface);
		addAttribute(ATTR_PORT, port);
	}

	public InterfaceStoppedEvent(Object source, String resourceKey,
			InterfaceResource resource, Throwable e, Session session,
			String boundInterface, int port) {
		super(source, resourceKey, resource, e, session, boundInterface);
		addAttribute(ATTR_PORT, port);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
