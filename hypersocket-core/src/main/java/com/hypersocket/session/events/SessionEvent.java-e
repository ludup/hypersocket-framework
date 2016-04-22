package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class SessionEvent extends SystemEvent {

	private static final long serialVersionUID = 4343611840171821823L;

	public static final String EVENT_RESOURCE_KEY = "session.event";
	
	public static final String ATTR_UUID = "attr.uuid";
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PRINCIPAL_DESC = "attr.principalDesc";
	public static final String ATTR_PRINCIPAL_REALM = CommonAttributes.ATTR_PRINCIPAL_REALM;
	public static final String ATTR_IP_ADDRESS = CommonAttributes.ATTR_IP_ADDRESS;
	
	Session session;
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session) {
		this(source, resourceKey, success, session, session.getCurrentRealm());
	}
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session, Realm realm) {
		super(source, resourceKey, success, realm);
		this.session = session;
		addAttribute(ATTR_UUID, session.getId());
		addAttribute(ATTR_PRINCIPAL_NAME, session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_PRINCIPAL_DESC, session.getCurrentPrincipal().getPrincipalDescription());
		addAttribute(ATTR_PRINCIPAL_REALM, realm.getName());
		addAttribute(ATTR_IP_ADDRESS, session.getRemoteAddress());
	}
	
	public SessionEvent(Object source, String resourceKey, Throwable e,
			Session session) {
		this(source, resourceKey, e, session, session.getCurrentRealm());
	}
	
	public SessionEvent(Object source, String resourceKey, Throwable e,
			Session session, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
		this.session = session;
		addAttribute(ATTR_UUID, session.getId());
		addAttribute(ATTR_PRINCIPAL_NAME, session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_PRINCIPAL_DESC, session.getCurrentPrincipal().getPrincipalDescription());
		addAttribute(ATTR_PRINCIPAL_REALM, session.getCurrentRealm().getName());
		addAttribute(ATTR_IP_ADDRESS, session.getRemoteAddress());
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
