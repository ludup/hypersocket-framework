package com.hypersocket.server.events;

import com.hypersocket.realm.Realm;

public class ServerStoppedEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = 2199176889846404649L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStopped";

	public ServerStoppedEvent(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}
}
