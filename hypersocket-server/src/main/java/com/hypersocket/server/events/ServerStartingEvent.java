package com.hypersocket.server.events;

public class ServerStartingEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = 2326113440143956901L;

	public final static String EVENT_RESOURCE_KEY = "server.starting";
	
	public ServerStartingEvent(Object source) {
		super(source, EVENT_RESOURCE_KEY, true);
	}

}
