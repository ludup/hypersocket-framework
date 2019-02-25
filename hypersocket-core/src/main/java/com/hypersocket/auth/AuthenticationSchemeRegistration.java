package com.hypersocket.auth;

public interface AuthenticationSchemeRegistration {

	String getResourceKey();
	
	boolean isEnabled();
}
