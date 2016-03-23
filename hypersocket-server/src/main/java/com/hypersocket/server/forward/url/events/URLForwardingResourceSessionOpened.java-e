package com.hypersocket.server.forward.url.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.forward.url.URLForwardingResource;
import com.hypersocket.session.Session;

public class URLForwardingResourceSessionOpened extends URLForwardingResourceSessionEvent {

	private static final long serialVersionUID = -904225011629709870L;

	public static final String EVENT_RESOURCE_KEY = "url.sessionOpened";
	
	public URLForwardingResourceSessionOpened(Object source,
			boolean success, URLForwardingResource resource, Session session) {
		super(source, EVENT_RESOURCE_KEY, success, resource, session);

	}

	public URLForwardingResourceSessionOpened(Object source,
			Throwable e, URLForwardingResource resource, Session session,String hostname) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session,hostname);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public boolean isUsage() {
		return true;
	}
}
