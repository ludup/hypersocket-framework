package com.hypersocket.auth;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.realm.Realm;

public class AuthenticationEvent extends SystemEvent {

	private static final long serialVersionUID = -1557924699852329686L;
	
	public static final String ATTR_IP_ADDRESS = CommonAttributes.ATTR_IP_ADDRESS;
	public static final String ATTR_SCHEME = CommonAttributes.ATTR_SCHEME;
	public static final String ATTR_MODULE = CommonAttributes.ATTR_MODULE;
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PRINCIPAL_REALM = CommonAttributes.ATTR_PRINCIPAL_REALM;
	public static final String ATTR_HINT = CommonAttributes.ATTR_HINT;
	
	public static final String EVENT_RESOURCE_KEY = "event.auth";
	
	public AuthenticationEvent(Object source, AuthenticationState state, Authenticator authenticator) {
		this(source, true, state, authenticator);
	}
	
	public AuthenticationEvent(Object source, AuthenticationState state, Authenticator authenticator, String resourceKey) {
		this(source, false, state, authenticator);
		addAttribute(AuthenticationEvent.ATTR_HINT, 
				I18NServiceImpl.tagForConversion(
						AuthenticationService.RESOURCE_BUNDLE, 
						resourceKey));
	}
	
	private AuthenticationEvent(Object source,
			boolean success, AuthenticationState state, Authenticator authenticator) {
		super(source, EVENT_RESOURCE_KEY, success, state.getRealm());
		addAttribute(AuthenticationEvent.ATTR_IP_ADDRESS, 
				state.getRemoteAddress());
		addAttribute(AuthenticationEvent.ATTR_SCHEME, 
				state.getScheme().getName());
		addAttribute(AuthenticationEvent.ATTR_MODULE, 
				I18NServiceImpl.tagForConversion(
				authenticator.getResourceBundle(),  
				authenticator.getResourceKey()));
		addAttribute(AuthenticationEvent.ATTR_PRINCIPAL_NAME, 
				state.getLastPrincipalName());
		addAttribute(AuthenticationEvent.ATTR_PRINCIPAL_REALM, 
				state.getLastRealmName());
	}

	public AuthenticationEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
	}
	
	public String getResourceBundle() {
		return AuthenticationService.RESOURCE_BUNDLE;
	}

}
