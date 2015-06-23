package com.hypersocket.interfaceState.event;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.session.Session;

public class UserInterfaceStateDeletedEvent extends UserInterfaceStateEvent {

	private static final long serialVersionUID = -5403540904569392942L;

	public static final String EVENT_RESOURCE_KEY = "event.stateDeleted";

	public UserInterfaceStateDeletedEvent(Object source, Session session,
			UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, true, session, state);
	}

	public UserInterfaceStateDeletedEvent(Object source, Throwable e,
			Session session, UserInterfaceState state) {
		super(source, EVENT_RESOURCE_KEY, e, session, state);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
