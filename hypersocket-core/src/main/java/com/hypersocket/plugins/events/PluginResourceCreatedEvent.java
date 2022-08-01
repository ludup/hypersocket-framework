package com.hypersocket.plugins.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.plugins.PluginResource;
import com.hypersocket.session.Session;

@SuppressWarnings("serial")
public class PluginResourceCreatedEvent extends
		PluginResourceEvent {

	/**
	 * TODO You typically add attributes to the base PluginResourceEvent class
	 * so these can be reused across all resource events.
	 */
	public static final String EVENT_RESOURCE_KEY = "plugin.created";
	
	public PluginResourceCreatedEvent(Object source,
			Session session,
			PluginResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public PluginResourceCreatedEvent(Object source,
			PluginResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
