package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class SessionEvent extends SystemEvent {

	private static final long serialVersionUID = 4343611840171821823L;

	public static final String EVENT_RESOURCE_KEY = "session.event";
	
	public static final String ATTR_UUID = "attr.uuid";
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PRINCIPAL_REALM = CommonAttributes.ATTR_PRINCIPAL_REALM;
	public static final String ATTR_IP_ADDRESS = CommonAttributes.ATTR_IP_ADDRESS;
	
	Session session;
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session) {
		super(source, resourceKey, success, session.getCurrentRealm());
		this.session = session;
		addAttribute(ATTR_UUID, session.getId());
		addAttribute(ATTR_PRINCIPAL_NAME, session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_PRINCIPAL_REALM, session.getCurrentRealm().getName());
		addAttribute(ATTR_IP_ADDRESS, session.getRemoteAddress());
	}
	
	public SessionEvent(Object source, String resourceKey, Throwable e,
			Session session) {
		super(source, resourceKey, e, session.getCurrentPrincipal().getRealm());
		this.session = session;
	}
	
	public String getResourceBundle() {
		return SessionService.RESOURCE_BUNDLE;
	}

	public Session getSession() {
		return session;
	}

	public Principal getPrincipal() {
		return session.getCurrentPrincipal();
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
