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
	AuthenticationService authenticationService;
	
	@Autowired
	PermissionService permissionService; 
	
	public FallbackAuthenticator() {	
	}
	
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public boolean isHidden() {
		return true;
	}
	
	@Override
	protected boolean verifyCredentials(AuthenticationState state,
			Principal principal, @SuppressWarnings("rawtypes") Map parameters) {
		
		/**
		 * This is fallback for when authentication is broken. Only allow the 
		 * admin access to this authenticator.
		 */
		if(!permissionService.hasSystemPermission(principal)) {
			return false;
		}
		
		return super.verifyCredentials(state, principal, parameters);
	}
}
