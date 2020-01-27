package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;

public interface AuthenticatorExtender {

	AuthenticatorResult authenticate(AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters) throws AccessDeniedException;

	boolean canAuthenticate(Principal principal);
		
}
