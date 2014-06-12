/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;

@Component
public class UsernameAndPasswordAuthenticator implements Authenticator {

	public static final String RESOURCE_KEY = "usernameAndPassword";

	@Autowired
	RealmService realmService;

	@Autowired
	AuthenticationService authenticationService;

	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticator(this);
	}

	@Override
	public AuthenticatorResult authenticate(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException {

		String realmName = null;
		String username = AuthenticationUtils.getRequestParameter(parameters,
				UsernameAndPasswordTemplate.USERNAME_FIELD);
		String password = AuthenticationUtils.getRequestParameter(parameters,
				UsernameAndPasswordTemplate.PASSWORD_FIELD);

		if (username == null || username.equals("")) {
			username = state
					.getParameter(UsernameAndPasswordTemplate.USERNAME_FIELD);
		}

		if (password == null || password.equals("")) {
			password = state
					.getParameter(UsernameAndPasswordTemplate.PASSWORD_FIELD);
		}

		if (username == null || username.equals("")) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}
		
		if (password == null || password.equals("")) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}

		if (!parameters.containsKey(UsernameAndPasswordTemplate.REALM_FIELD)) {
			// Can we extract realm from username?
			int idx;
			idx = username.indexOf('@');
			if (idx > -1) {
				realmName = username.substring(idx + 1);
			} else {
				idx = username.indexOf('\\');
				if (idx > -1) {
					realmName = username.substring(0, idx);
				}
			}
		}

		if (realmName == null) {
			realmName = AuthenticationUtils.getRequestParameter(parameters,
					UsernameAndPasswordTemplate.REALM_FIELD);
			if (realmName == null) {
				realmName = state
						.getParameter(UsernameAndPasswordTemplate.REALM_FIELD);
			}
		}

		Realm realm = null;

		if (realmName != null) {
			realm = realmService.getRealmByName(realmName);
			if(realm==null) {
				return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_REALM;
			}
		}

		Principal principal = null;

		if (realm == null) {
			try {
				principal = realmService.getUniquePrincipal(username);
			} catch (ResourceNotFoundException e) {
				return AuthenticatorResult.INSUFFICIENT_DATA;
			}
			realm = principal.getRealm();
		} else {
			principal = realmService.getPrincipalByName(realm, username);	
		}
		
		if (principal == null) {
			state.setLastPrincipalName(username);
			state.setLastRealmName(realmName);
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_PRINCIPAL;
		}

		boolean result = realmService.verifyPassword(principal,
				password.toCharArray());

		if (result) {
			state.setRealm(realm);
			state.setPrincipal(principal);
		} else {
			state.setLastPrincipalName(username);
			state.setLastRealmName(realmName);
		}

		return result ? AuthenticatorResult.AUTHENTICATION_SUCCESS
				: AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public FormTemplate createTemplate(AuthenticationState state, Map params) {

		return new UsernameAndPasswordTemplate(true, true,
				realmService.allRealms(), state, params);
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public String getResourceBundle() {
		return AuthenticationService.RESOURCE_BUNDLE;
	}

	@Override
	public boolean containsSecret() {
		return true;
	}

	@Override
	public boolean containsIdentity() {
		return true;
	}

}
