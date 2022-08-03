package com.hypersocket.plugins.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.plugins.PluginResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

@SuppressWarnings("serial")
public class PluginResourceEvent extends SessionEvent {

	public static final String EVENT_RESOURCE_KEY = "plugin.event";

	private PluginResource resource;
	
	public static final String ATTR_PLUGIN_ID = "attr.pluginId";
	public static final String ATTR_PLUGIN_VERSION = "attr.pluginVersion";
	public static final String ATTR_PLUGIN_STATE = "attr.pluginState";
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;

	public PluginResourceEvent(Object source, String resourceKey, Session session, PluginResource resource) {
		super(source, resourceKey, true, session);
		this.resource = resource;
		addAttributes(resource);
	}

	public PluginResourceEvent(Object source, String resourceKey, PluginResource resource, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
		this.resource = resource;
		addAttributes(resource);
	}
	
	public PluginResource getResource() {
		return resource;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private void addAttributes(PluginResource resource) {
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		addAttribute(ATTR_PLUGIN_ID, resource.getId());
		addAttribute(ATTR_PLUGIN_VERSION, resource.getVersion());
		addAttribute(ATTR_PLUGIN_STATE, resource.getState().name());
	}
}
