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
import com.hypersocket.realm.RealmService;

@Component
public class UsernameAndPasswordAuthenticator extends
		AbstractUsernameAuthenticator {

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

	@Override
	public AuthenticationModuleType getType() {
		return AuthenticationModuleType.BASIC;
	}

	@Override
	protected boolean processFields(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters) {
		String password = AuthenticationUtils.getRequestParameter(parameters,
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
	protected boolean verifyCredentials(AuthenticationState state,
			Principal principal, @SuppressWarnings("rawtypes") Map parameters) {

		String password = AuthenticationUtils.getRequestParameter(parameters,
				UsernameAndPasswordTemplate.PASSWORD_FIELD);

		if (password == null || password.equals("")) {
			password = state
					.getParameter(UsernameAndPasswordTemplate.PASSWORD_FIELD);
		}
		return realmService.verifyPassword(principal, password.toCharArray());
	}

}
