package com.hypersocket.plugins.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.plugins.PluginResource;
import com.hypersocket.session.Session;

@SuppressWarnings("serial")
public class PluginResourceDeletedEvent extends
		PluginResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "plugin.deleted";
	public static final String ATTR_DELETE_DATA = "attr.deleteData";

	public PluginResourceDeletedEvent(Object source,
			Session session, PluginResource resource, boolean deleteData) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
		addAttribute(ATTR_DELETE_DATA, deleteData);
	}

	public PluginResourceDeletedEvent(Object source,
			PluginResource resource, Throwable e, Session session, boolean deleteData) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
		addAttribute(ATTR_DELETE_DATA, deleteData);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
