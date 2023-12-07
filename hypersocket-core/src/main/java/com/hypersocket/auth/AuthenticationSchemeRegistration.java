package com.hypersocket.auth;

import java.util.List;

import com.hypersocket.resource.ResourceException;

public interface AuthenticationSchemeRegistration {

	String getResourceKey();
	
	boolean isEnabled();
	
	boolean isAuthenticating();

	default void validate(List<String> modules) throws ResourceException {  };
}
