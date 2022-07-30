package com.hypersocket.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;

@Component
public class FallbackAuthenticator extends UsernameAndPasswordAuthenticator {

	public static final String RESOURCE_KEY = "usernameAndPasswordFallback";
	
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
			Principal principal, Map<String, String[]> parameters) {
		
		/**
		 * This is fallback for when authentication is broken. Only allow the 
		 * admin access to this authenticator.
		 */
		if(!permissionService.hasSystemPermission(principal)) {
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;
		}
		
		return super.verifyCredentials(state, principal, parameters);
	}
}
