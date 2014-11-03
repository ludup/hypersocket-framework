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


		Realm realm = authenticationService.resolveRealm(state, username);

		if(realm==null) {
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_REALM;
		}
		
		Principal principal =  realmService.getPrincipalByName(realm, username);

		if (principal == null) {

			if(username.indexOf('@') > -1) {
				username = username.substring(0, username.indexOf('@'));
			}
			
			principal = realmService.getPrincipalByName(realm, username);
			
			if(principal==null) {
				return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_PRINCIPAL;
			}
		}

		boolean result = realmService.verifyPassword(principal,
				password.toCharArray());

		if (result) {
			state.addParameter("password", password);
			state.setRealm(realm);
			state.setPrincipal(principal);
		} 

		return result ? AuthenticatorResult.AUTHENTICATION_SUCCESS
				: AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public FormTemplate createTemplate(AuthenticationState state, Map params) {

		return new UsernameAndPasswordTemplate(state, params);
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
	public boolean isSecretModule() {
		return true;
	}

	@Override
	public boolean isIdentityModule() {
		return true;
	}
	
	@Override
	public String[] getAllowedSchemes() {
		return new String[] { ".*" };
	}

}
