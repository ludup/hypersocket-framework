package com.hypersocket.auth;

public interface AuthenticatorSelector {
	Authenticator selectAuthenticator(AuthenticationState state, Authenticator auth);

	boolean isAuthenticatorOverridden(AuthenticationState state, Authenticator auth);
}
