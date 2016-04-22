package com.hypersocket.server.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;

public class ServerStartingEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = 2326113440143956901L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStarting";
	
	public ServerStartingEvent(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
