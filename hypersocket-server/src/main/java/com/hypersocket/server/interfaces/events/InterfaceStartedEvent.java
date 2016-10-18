package com.hypersocket.server.interfaces.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.InterfaceResource;
import com.hypersocket.session.Session;

public class InterfaceStartedEvent extends InterfaceResourceEvent {
	
	private static final long serialVersionUID = 8606170445099375558L;
	
	public static final String EVENT_RESOURCE_KEY = "interfaceStopped.event";
	
	public InterfaceStartedEvent(Object source, String resourceKey,
			Session session, InterfaceResource resource, String boundInterface) {
		super(source, resourceKey, session, resource, boundInterface);
	}

	public InterfaceStartedEvent(Object source, String resourceKey,
			InterfaceResource resource, Throwable e, Session session, String boundInterface) {
		super(source, resourceKey, resource, e, session, boundInterface);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
