package com.hypersocket.server.interfaces.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.server.interfaces.InterfaceResource;
import com.hypersocket.session.Session;

public class InterfaceResourceEvent extends ResourceEvent {

	private static final long serialVersionUID = -5515325639125356195L;
	
	public static final String EVENT_RESOURCE_KEY = "interface.event";
	
	public static final String ATTR_INTERFACES = "attr.interfaces";
	public static final String ATTR_PORT = "attr.port";
	
	public InterfaceResourceEvent(Object source, String resourceKey,
			Session session, InterfaceResource resource, String boundInterface) {
		super(source, resourceKey, true, session, resource);
		addAttribute(ATTR_INTERFACES, boundInterface);
		addAttribute(ATTR_PORT, String.valueOf(resource.getPort()));
		
	}
	
	public InterfaceResourceEvent(Object source, String resourceKey,
			Session session, InterfaceResource resource) {
		super(source, resourceKey, true, session, resource);
		addAttribute(ATTR_INTERFACES, ResourceUtils.createDelimitedString(ResourceUtils.explodeCollectionValues(resource.getInterfaces()), "\r\n"));
		addAttribute(ATTR_PORT, String.valueOf(resource.getPort()));
		
	}
	public InterfaceResourceEvent(Object source, String resourceKey,
			InterfaceResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		addAttribute(ATTR_INTERFACES, ResourceUtils.createDelimitedString(ResourceUtils.explodeCollectionValues(resource.getInterfaces()), "\r\n"));
		addAttribute(ATTR_PORT, String.valueOf(resource.getPort()));
	}
	
	public InterfaceResourceEvent(Object source, String resourceKey,
			InterfaceResource resource, Throwable e, Session session, String boundInterface) {
		super(source, resourceKey, e, session, resource);
		addAttribute(ATTR_INTERFACES, boundInterface);
		addAttribute(ATTR_PORT, String.valueOf(resource.getPort()));
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
