package com.hypersocket.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;

public class CoreStartedEvent extends SystemEvent {

	private static final long serialVersionUID = -3913157834293856133L;

	public final static String EVENT_RESOURCE_KEY = "event.coreStarted";

	public CoreStartedEvent(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	@Override
	public String getResourceBundle() {
		return EventServiceImpl.RESOURCE_BUNDLE;
	}
}
