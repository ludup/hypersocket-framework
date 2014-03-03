package com.hypersocket.server.events;

public class ServerStoppedEvent extends HypersocketServerEvent {

	private static final long serialVersionUID = 2199176889846404649L;

	public final static String EVENT_RESOURCE_KEY = "event.serverStopped";

	public ServerStoppedEvent(Object source) {
		super(source, EVENT_RESOURCE_KEY, true);
	}
}
