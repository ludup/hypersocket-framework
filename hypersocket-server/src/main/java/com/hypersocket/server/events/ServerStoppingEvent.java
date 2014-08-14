package com.hypersocket.server.events;

import com.hypersocket.realm.Realm;

public class ServerStoppingEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = -302922543628681693L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStopping";
	
	public ServerStoppingEvent(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}


}
