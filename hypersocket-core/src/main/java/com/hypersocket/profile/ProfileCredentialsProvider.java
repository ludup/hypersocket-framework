package com.hypersocket.profile;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;

public interface ProfileCredentialsProvider {

	String getResourceKey();
	
	ProfileCredentialsState hasCredentials(Principal principal) throws AccessDeniedException;
}
