package com.hypersocket.auth;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.realm.Realm;

public class AuthenticationAttemptEvent extends SystemEvent {

	private static final long serialVersionUID = -1557924699852329686L;
	
	public static final String ATTR_IP_ADDRESS = CommonAttributes.ATTR_IP_ADDRESS;
	public static final String ATTR_SCHEME = CommonAttributes.ATTR_SCHEME;
	public static final String ATTR_MODULE = CommonAttributes.ATTR_MODULE;
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PRINCIPAL_REALM = CommonAttributes.ATTR_PRINCIPAL_REALM;
	public static final String ATTR_HINT = CommonAttributes.ATTR_HINT;
	
	public static final String EVENT_RESOURCE_KEY = "event.auth";
	
	public AuthenticationAttemptEvent(Object source, AuthenticationState state, Authenticator authenticator) {
		this(source, true, state, authenticator);
	}
	
	public AuthenticationAttemptEvent(Object source, AuthenticationState state, Authenticator authenticator, String resourceKey) {
		this(source, false, state, authenticator);
		addAttribute(AuthenticationAttemptEvent.ATTR_HINT, 
				I18NServiceImpl.tagForConversion(
						AuthenticationService.RESOURCE_BUNDLE, 
						resourceKey));
	}
	
	private AuthenticationAttemptEvent(Object source,
			boolean success, AuthenticationState state, Authenticator authenticator) {
		super(source, EVENT_RESOURCE_KEY, success, state.getRealm());
		addAttribute(AuthenticationAttemptEvent.ATTR_IP_ADDRESS, 
				state.getRemoteAddress());
		addAttribute(AuthenticationAttemptEvent.ATTR_SCHEME, 
				state.getScheme().getName());
		addAttribute(AuthenticationAttemptEvent.ATTR_MODULE, 
				I18NServiceImpl.tagForConversion(
				authenticator.getResourceBundle(),  
				authenticator.getResourceKey()));
		addAttribute(AuthenticationAttemptEvent.ATTR_PRINCIPAL_NAME, 
				state.getLastPrincipalName());
		addAttribute(AuthenticationAttemptEvent.ATTR_PRINCIPAL_REALM, 
				state.getLastRealmName());
	}

	public AuthenticationAttemptEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
	}
	
	public String getResourceBundle() {
		return AuthenticationService.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
