package com.hypersocket.server.forward.url.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.resource.ResourceSessionEvent;
import com.hypersocket.server.forward.ForwardingResource;
import com.hypersocket.server.forward.url.URLForwardingResource;
import com.hypersocket.server.forward.url.URLForwardingResourceServiceImpl;
import com.hypersocket.session.Session;

public abstract class URLForwardingResourceSessionEvent extends ResourceSessionEvent {

	private static final long serialVersionUID = -3240036750533492092L;

	public static final String EVENT_RESOURCE_KEY = "urlSession.event";
	
	public static final String ATTR_URL = "attr.url";
	
	public URLForwardingResourceSessionEvent(Object source, String resourceKey,
			boolean success, URLForwardingResource resource, Session session) {
		super(source, resourceKey, success, session, resource);
		addAttribute(ATTR_URL, resource.getLaunchUrl());
	}

	public URLForwardingResourceSessionEvent(Object source, String resourceKey, ForwardingResource resource, Throwable e,
			Session session,String hostname) {
		super(source, resourceKey, resource.getName(), e, session);
		addAttribute(ATTR_URL, hostname);
	}

	@Override
	public String getResourceBundle() {
		return URLForwardingResourceServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
