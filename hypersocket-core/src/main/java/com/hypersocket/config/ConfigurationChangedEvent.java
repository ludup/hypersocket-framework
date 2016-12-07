package com.hypersocket.config;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ConfigurationChangedEvent extends SessionEvent {

	private static final long serialVersionUID = 5849555055879289458L;

	public static final String EVENT_RESOURCE_KEY = "config.changeComplete";

	public ConfigurationChangedEvent(Object source, boolean success,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, success, session);
	}

	public ConfigurationChangedEvent(Object source, String resourceKey,
			Throwable e, Session session) {
		super(source, resourceKey, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}
