package com.hypersocket.server.events;

public class ServerStoppingEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = -302922543628681693L;

	public final static String EVENT_RESOURCE_KEY = "server.stopping";
	
	public ServerStoppingEvent(Object source) {
		super(source, EVENT_RESOURCE_KEY, true);
	}


}
