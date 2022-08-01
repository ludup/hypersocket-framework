package com.hypersocket.plugins.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.plugins.PluginResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

@SuppressWarnings("serial")
public class PluginResourceEvent extends SessionEvent {

	public static final String EVENT_RESOURCE_KEY = "plugin.event";

	private PluginResource resource;

	public PluginResourceEvent(Object source, String resourceKey, Session session, PluginResource resource) {
		super(source, resourceKey, true, session);
		this.resource = resource;
	}

	public PluginResourceEvent(Object source, String resourceKey, PluginResource resource, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		this.resource = resource;
	}
	
	public PluginResource getResource() {
		return resource;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
