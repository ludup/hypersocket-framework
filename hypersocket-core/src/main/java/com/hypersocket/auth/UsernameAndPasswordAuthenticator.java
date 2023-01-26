/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.realm.LogonException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.util.ArrayValueHashMap;

@Component
public class UsernameAndPasswordAuthenticator extends
		AbstractUsernameAuthenticator {

	public static final String RESOURCE_KEY = "usernameAndPassword";

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private RealmService realmService;

	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticator(this);
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state, Map<String, String[]> params) {
		return new UsernameAndPasswordTemplate(state, params, getLogonRealms(state),
				realmService.getDefaultRealm());
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

	@Override
	public AuthenticationModuleType getType() {
		return AuthenticationModuleType.BASIC;
	}

	@Override
	protected boolean processFields(AuthenticationState state,
			Map<String, String[]> parameters) {
		String password = ArrayValueHashMap.getSingle(parameters,
				UsernameAndPasswordTemplate.PASSWORD_FIELD);

		if (password == null || password.equals("")) {
			password = state
					.getParameter(UsernameAndPasswordTemplate.PASSWORD_FIELD);
		}

		if (password == null || password.equals("")) {
			return false;
		}

		return true;
	}

	@Override
	protected AuthenticatorResult verifyCredentials(AuthenticationState state,
			Principal principal, Map<String, String[]> parameters) {

		String password = ArrayValueHashMap.getSingle(parameters,
				UsernameAndPasswordTemplate.PASSWORD_FIELD);

		if (password == null || password.equals("")) {
			password = state
					.getParameter(UsernameAndPasswordTemplate.PASSWORD_FIELD);
		}
		
		boolean result = false;
		try {
			result = realmService.verifyPassword(principal, password.toCharArray());
			if(result) {
				state.addParameter("password", password);
			}
			return result ? AuthenticatorResult.AUTHENTICATION_SUCCESS
					: AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_CREDENTIALS;
		} catch (IOException | LogonException e) {
			state.setLastErrorIsResourceKey(false);
			state.setLastErrorMsg(e.getMessage());
			return AuthenticatorResult.AUTHENTICATION_FAILURE_DISPLAY_ERROR;
		}
	}

	@Override
	public AbstractResourceRepository<AuthenticationScheme> getRepository() {
		return null;
	}
	
	@Override
	public boolean isPropertiesModule() {
		return false;
	}
	
	@Override
	public boolean requiresUserInput(AuthenticationState state) {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getCredentialsResourceKey() {
		return "password";
	}

	@Override
	public AuthenticationModuleCategory getAuthenticationModuleCategory() {
		return AuthenticationModuleCategory.IDENTITY_SECRET;
	}
}
