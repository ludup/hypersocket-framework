package com.hypersocket.plugins.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.plugins.PluginResource;
import com.hypersocket.session.Session;

@SuppressWarnings("serial")
public class PluginResourceDeletedEvent extends
		PluginResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "plugin.deleted";

	public PluginResourceDeletedEvent(Object source,
			Session session, PluginResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public PluginResourceDeletedEvent(Object source,
			PluginResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
