package com.hypersocket.server.events;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.server.HypersocketServer;

public abstract class HypersocketServerEvent extends SystemEvent {

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

}
