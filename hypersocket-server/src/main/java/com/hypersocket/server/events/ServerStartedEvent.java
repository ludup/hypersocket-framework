package com.hypersocket.server.events;

public class ServerStartedEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = -3913157834293856133L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStarted";

	public ServerStartedEvent(Object source) {
		super(source, EVENT_RESOURCE_KEY, true);
	}

}
