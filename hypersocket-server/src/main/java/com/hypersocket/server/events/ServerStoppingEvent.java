package com.hypersocket.server.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;

public class ServerStoppingEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = -302922543628681693L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStopping";
	
	public ServerStoppingEvent(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
