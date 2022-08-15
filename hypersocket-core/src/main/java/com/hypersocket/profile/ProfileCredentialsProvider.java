package com.hypersocket.profile;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.ResourceException;

public interface ProfileCredentialsProvider {

	String getResourceKey();
	
	ProfileCredentialsState hasCredentials(Principal principal, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;
	
	default void deleteCredentials(Principal principal) throws AccessDeniedException, ResourceException { };
}
