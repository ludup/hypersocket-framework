package com.hypersocket.server.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;

public class WebappCreatedEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = -4644461545912219190L;

	public final static String EVENT_RESOURCE_KEY = "event.webappCreated";
	
	public WebappCreatedEvent(Object source,
			boolean success, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
}
