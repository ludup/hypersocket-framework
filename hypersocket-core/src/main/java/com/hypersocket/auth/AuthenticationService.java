/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public interface AuthenticationService extends AuthenticatedService {

	public static final String RESOURCE_BUNDLE = "AuthenticationService";

	void registerAuthenticator(Authenticator authenticator);

	AuthenticationScheme getDefaultScheme(String remoteAddress,
			Map<String, String> environment, Realm realm);

	AuthenticationState createAuthenticationState(String scheme,
			String remoteAddress, Map<String, Object> environment, Locale locale)
			throws AccessDeniedException;

	@SuppressWarnings("rawtypes")
	void logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException;

	@SuppressWarnings("rawtypes")
	FormTemplate nextAuthenticationTemplate(AuthenticationState state,
			Map params);

	Session completeLogon(AuthenticationState state)
			throws AccessDeniedException;

	FormTemplate nextPostAuthenticationStep(AuthenticationState state);

	void registerPostAuthenticationStep(
			PostAuthenticationStep postAuthenticationStep);

	public List<AuthenticationScheme> getAuthenticationSchemes()
			throws AccessDeniedException;

	public List<AuthenticationModule> getAuthenticationModules()
			throws AccessDeniedException;

	public List<AuthenticationModule> getAuthenticationModulesByScheme(
			AuthenticationScheme authenticationScheme)
			throws AccessDeniedException;

	public AuthenticationModule getModuleById(Long id)
			throws AccessDeniedException;

	public AuthenticationScheme getSchemeById(Long id)
			throws AccessDeniedException;

	public void updateSchemeModules(List<AuthenticationModule> moduleList)
			throws AccessDeniedException;

	public AuthenticationModule createAuthenticationModule(
			AuthenticationModule authenticationModule)
			throws AccessDeniedException;

	public AuthenticationModule updateAuthenticationModule(
			AuthenticationModule authenticationModule)
			throws AccessDeniedException;

	public void deleteModule(AuthenticationModule authenticationModule)
			throws AccessDeniedException;

	public void deleteModulesByScheme(AuthenticationScheme authenticationScheme)
			throws AccessDeniedException;

	public Map<String,Authenticator> getAuthenticators();

	void registerListener(AuthenticationServiceListener listener);
}
