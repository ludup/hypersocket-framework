package com.hypersocket.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;


public abstract class AbstractUsernameAuthenticator implements Authenticator {

	@Autowired
	RealmService realmService;

	@Autowired
	AuthenticationService authenticationService;

	@Override
	public AuthenticatorResult authenticate(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException {

		String username = AuthenticationUtils.getRequestParameter(parameters,
				UsernameAndPasswordTemplate.USERNAME_FIELD);

		if (username == null || username.equals("")) {
			username = state
					.getParameter(UsernameAndPasswordTemplate.USERNAME_FIELD);
		}

		if (username == null || username.equals("")) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}

		if(!processFields(state, parameters)) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}

		try {
			Principal principal = authenticationService.resolvePrincipalAndRealm(
					state, username);

			boolean result = verifyCredentials(state, principal, parameters);

			if (result) {
				state.setRealm(principal.getRealm());
				state.setPrincipal(principal);
			}

			return result ? AuthenticatorResult.AUTHENTICATION_SUCCESS
					: AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;
		} catch (PrincipalNotFoundException e) {
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_PRINCIPAL;
		}

	}
	
	protected abstract boolean processFields(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters);

	protected abstract boolean verifyCredentials(AuthenticationState state,
			Principal principal, 
			@SuppressWarnings("rawtypes") Map parameters);
	
	public boolean isHidden() {
		return false;
	}

}
