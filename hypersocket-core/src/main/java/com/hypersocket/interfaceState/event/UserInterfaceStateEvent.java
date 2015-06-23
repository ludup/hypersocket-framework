package com.hypersocket.interfaceState.event;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class UserInterfaceStateEvent extends ResourceEvent {

	private static final long serialVersionUID = 7032475746147901955L;

	public static final String EVENT_RESOURCE_KEY = "state.event";
	public static final String ATTR_STATE_NAME = "attr.stateName";
	public static final String ATTR_STATE_TOP = "attr.stateTop";
	public static final String ATTR_STATE_LEFT = "attr.stateLeft";

	public UserInterfaceStateEvent(Object source, String resourceKey,
			boolean success, Session session, UserInterfaceState state) {
		super(source, resourceKey, true, session, state);
		addAttribute(ATTR_STATE_NAME, state.getName());
		addAttribute(ATTR_STATE_TOP, state.getTop());
		addAttribute(ATTR_STATE_LEFT, state.getLeftpx());
	}

	public UserInterfaceStateEvent(Object source, String resourceKey,
			Throwable e, Session session, UserInterfaceState state) {
		super(source, resourceKey, e, session, state);
		addAttribute(ATTR_STATE_NAME, state.getName());
		addAttribute(ATTR_STATE_TOP, state.getTop());
		addAttribute(ATTR_STATE_LEFT, state.getLeftpx());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
