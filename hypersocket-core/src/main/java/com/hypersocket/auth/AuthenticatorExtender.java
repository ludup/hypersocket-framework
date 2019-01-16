package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;

public interface AuthenticatorExtender {

	public AuthenticatorResult authenticate(AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters) throws AccessDeniedException;
		
}
