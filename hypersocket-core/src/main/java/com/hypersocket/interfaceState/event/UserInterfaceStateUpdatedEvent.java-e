package com.hypersocket.interfaceState.event;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.session.Session;

public class UserInterfaceStateUpdatedEvent extends UserInterfaceStateEvent {

	private static final long serialVersionUID = -7998808224130079121L;
	
	public static final String EVENT_RESOURCE_KEY = "event.stateUpdated";

	public UserInterfaceStateUpdatedEvent(Object source, Session session,
			String oldName, UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, true, session, state);
	}

	public UserInterfaceStateUpdatedEvent(Object source, Throwable e,
			Session session, UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, e, session, state);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
