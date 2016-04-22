package com.hypersocket.server.forward.url.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.forward.url.URLForwardingResource;
import com.hypersocket.session.Session;

public class URLForwardingResourceSessionClosed extends URLForwardingResourceSessionEvent {

	private static final long serialVersionUID = -5550548229601776898L;

	public static final String EVENT_RESOURCE_KEY = "url.sessionClosed";
	
	public static final String ATTR_BYTES_IN = "attr.totalBytesIn";
	public static final String ATTR_BYTES_OUT = "attr.totalBytesOut";
	
	public URLForwardingResourceSessionClosed(Object source,
			URLForwardingResource resource, Session session, long totalBytesIn,
			long totalBytesOut) {
		super(source, EVENT_RESOURCE_KEY, true, resource, session);
		addAttribute(ATTR_BYTES_IN, String.valueOf(totalBytesIn));
		addAttribute(ATTR_BYTES_OUT, String.valueOf(totalBytesIn));
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public boolean isUsage() {
		return false;
	}
}
