package com.hypersocket.server.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.server.HypersocketServer;

public abstract class HypersocketServerEvent extends SystemEvent {

	public static final String EVENT_RESOURCE_KEY = "server.event";
	
	private static final long serialVersionUID = 6303299116450047409L;

	public HypersocketServerEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
	}

	public HypersocketServerEvent(Object source, String resourceKey,
			boolean success, Realm currentRealm) {
		super(source, resourceKey, success, currentRealm);
	}

	@Override
	public String getResourceBundle() {
		return HypersocketServer.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
