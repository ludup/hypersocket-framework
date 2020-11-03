package com.hypersocket.server;

public interface HomePageResolver {
	
	public enum AuthenticationRequirements {
		AUTHENTICATED, NOT_AUTHENTICATED, ANY
	}

	String getHomePage();
	
	default AuthenticationRequirements getAuthenticationRequirements() {
		return AuthenticationRequirements.ANY;
	}
	 
}
