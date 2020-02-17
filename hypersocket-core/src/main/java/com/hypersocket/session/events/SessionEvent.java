package com.hypersocket.session.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.AuthenticationService;
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
	public static final String ATTR_IMPERSONATOR = "attr.impersonator";
	public static final String ATTR_IMPERSONATOR_DESC = "attr.impersonatorDesc";
	public static final String ATTR_PRINCIPAL_REALM = CommonAttributes.ATTR_PRINCIPAL_REALM;
	public static final String ATTR_IP_ADDRESS = CommonAttributes.ATTR_IP_ADDRESS;
	
	private Session session;
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session) {
		this(source, resourceKey, success, session, session.getCurrentRealm(), false);
	}
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session, boolean hidden) {
		this(source, resourceKey, success, session, session.getCurrentRealm(), hidden);
	}
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session, Realm realm) {
		this(source, resourceKey, success, session, realm, false);
	}
	
	public SessionEvent(Object source, String resourceKey, boolean success,
			Session session, Realm currentRealm, boolean hidden) {
		super(source, resourceKey, success, currentRealm);
		this.session = session;
		this.hidden = hidden;
		addAttributes(session, currentRealm);
	}
	
	public SessionEvent(Object source, String resourceKey, Throwable e,
			Session session) {
		this(source, resourceKey, e, session, session.getCurrentRealm());
	}
	
	public SessionEvent(Object source, String resourceKey, Throwable e,
			Session session, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
		this.session = session;
		addAttributes(session, currentRealm);
	}
	
	private void addAttributes(Session session, Realm currentRealm) {
		
		addAttribute(ATTR_UUID, session.getId());
		
		addAttribute(ATTR_PRINCIPAL_NAME, session.getCurrentPrincipal().getPrincipalName());
		addAttribute(ATTR_PRINCIPAL_DESC, session.getCurrentPrincipal().getDescription());
		
		addAttribute(ATTR_PRINCIPAL_REALM, currentRealm.getName());
		addAttribute(ATTR_IP_ADDRESS, session.getRemoteAddress());
		
		if(session.isImpersonating()) {
			addAttribute(ATTR_IMPERSONATOR, session.getInheritedPrincipal().getPrincipalName());
			addAttribute(ATTR_IMPERSONATOR_DESC, session.getInheritedPrincipal().getDescription());
		}
	}
	
	public String getResourceBundle() {
		return SessionService.RESOURCE_BUNDLE;
	}

	public Session getSession() {
		return session;
	}

	public Principal getTargetPrincipal() {
		return session.getCurrentPrincipal();
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
