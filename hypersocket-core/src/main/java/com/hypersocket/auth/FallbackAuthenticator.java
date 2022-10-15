package com.hypersocket.auth;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;

@Component
public class FallbackAuthenticator extends UsernameAndPasswordAuthenticator {

	public static final String RESOURCE_KEY = "usernameAndPasswordFallback";
	
	private static final Logger log = LoggerFactory.getLogger(FallbackAuthenticator.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	public FallbackAuthenticator() {	
	}
	
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public boolean isHidden() {
		return true;
	}
	
	@Override
	protected AuthenticatorResult verifyCredentials(AuthenticationState state,
			Principal principal, @SuppressWarnings("rawtypes") Map parameters) {
		
		/**
		 * This is fallback for when authentication is broken. Only allow the 
		 * admin access to this authenticator.
		 */
		if(!permissionService.hasSystemPermission(principal)) {
			log.warn("Recovery denied for user {}", principal.getPrincipalName());
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;
		}
		
		if(!state.getRemoteAddress().equals("127.0.0.1")) {
			log.warn("Recovery denied for IP address {}", state.getRemoteAddress());
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;
		}
		
		return super.verifyCredentials(state, principal, parameters);
	}
}
