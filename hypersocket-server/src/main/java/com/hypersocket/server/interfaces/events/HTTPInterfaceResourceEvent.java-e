package com.hypersocket.server.interfaces.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.server.interfaces.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceEvent extends ResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.event";
	
	public HTTPInterfaceResourceEvent(Object source, String resourceKey,
			Session session, HTTPInterfaceResource resource) {
		super(source, resourceKey, true, session, resource);

		/**
		 * TODO add attributes of your resource here. Make sure all attributes
		 * have a constant string definition like the commented out example above,
		 * its important for its name to start with ATTR_ as this is picked up during 
		 * the registration process
		 */
	}

	public HTTPInterfaceResourceEvent(Object source, String resourceKey,
			HTTPInterfaceResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
