package com.hypersocket.profile;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.ResourceException;

public interface ProfileCredentialsProvider {

	String getResourceKey();
	
	ProfileCredentialsState hasCredentials(Principal principal) throws AccessDeniedException;
	
	default void deleteCredentials(Principal principal) throws AccessDeniedException, ResourceException { };
}
