package com.hypersocket.server.interfaces.http.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.server.interfaces.events.InterfaceResourceEvent;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.session.Session;

public class HTTPInterfaceResourceEvent extends InterfaceResourceEvent {

	private static final long serialVersionUID = -5515325639125356195L;
	
	public static final String EVENT_RESOURCE_KEY = "hTTPInterface.event";
	
	public static final String ATTR_INTERFACES = "attr.interfaces";
	public static final String ATTR_PORT = "attr.port";
	public static final String ATTR_PROTOCOL = "attr.protocol";
	public static final String ATTR_CERTIFICATE = "attr.certificate";
	public static final String ATTR_REDIRECT = "attr.redirect";
	public static final String ATTR_REDIRECT_PORT = "attr.redirectPort";
	
	public HTTPInterfaceResourceEvent(Object source, String resourceKey,
			Session session, HTTPInterfaceResource resource) {
		super(source, resourceKey, session, resource);
		addAttribute(ATTR_PROTOCOL, resource.getProtocol().toString());
		switch(resource.getProtocol()) {
		case HTTPS:
			addAttribute(ATTR_CERTIFICATE, resource.getCertificate().getName());
			break;
		default:
			addAttribute(ATTR_REDIRECT, String.valueOf(resource.getRedirectHTTPS()));
			addAttribute(ATTR_REDIRECT_PORT, String.valueOf(resource.getRedirectPort()));
			break;
		}
	}

	public HTTPInterfaceResourceEvent(Object source, String resourceKey,
			HTTPInterfaceResource resource, Throwable e, Session session) {
		super(source, resourceKey, resource, e, session);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
