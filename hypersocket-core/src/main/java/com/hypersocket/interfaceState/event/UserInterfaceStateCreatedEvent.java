package com.hypersocket.interfaceState.event;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.session.Session;

public class UserInterfaceStateCreatedEvent extends UserInterfaceStateEvent {

	private static final long serialVersionUID = -7238655519436082121L;

	public static final String EVENT_RESOURCE_KEY = "event.stateCreated";

	public UserInterfaceStateCreatedEvent(Object source, Session session,
			UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, true, session, state);
	}

	public UserInterfaceStateCreatedEvent(Object source, Throwable e,
			Session session, UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, e, session, state);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
