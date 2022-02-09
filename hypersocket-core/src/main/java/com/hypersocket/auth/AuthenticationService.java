/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public interface AuthenticationService extends PasswordEnabledAuthenticatedService {

	public static final String RESOURCE_BUNDLE = "AuthenticationService";
	public static final String AUTHENTICATION_SCHEME = "authenticationScheme";
	public String LOGONBOX_ENCRYPTED_PASSWORD = "logonbox.encryptedPassword";

	void registerAuthenticator(Authenticator authenticator);

	AuthenticationScheme getDefaultScheme(String remoteAddress,
			Map<String, Object> environment, Realm realm);

	AuthenticationState createAuthenticationState(String scheme,
			String remoteAddress, Map<String, Object> environment, Realm realm, Locale locale)
			throws AccessDeniedException;

	@SuppressWarnings("rawtypes")
	boolean logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException, FallbackAuthenticationRequired;

	@SuppressWarnings("rawtypes")
	FormTemplate nextAuthenticationTemplate(AuthenticationState state,
			Map params);

	Session completeLogon(AuthenticationState state)
			throws AccessDeniedException;

	FormTemplate nextPostAuthenticationStep(AuthenticationState state) throws AccessDeniedException;

	void registerPostAuthenticationStep(
			PostAuthenticationStep postAuthenticationStep);

	Map<String,Authenticator> getAuthenticators(String scheme);

	void registerListener(AuthenticationServiceListener listener);

	boolean isAuthenticatorInScheme(Realm realm, String schemeResourceKey, String resourceKey);

	boolean isAuthenticatorInUse(Realm realm, String resourceKey);
	
	AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey) throws AccessDeniedException;
	
	AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey, boolean allowFallback) throws AccessDeniedException;

	Session logonAnonymous(String remoteAddress,
			String userAgent, 
			Map<String, String> parameters, 
			String serverName) throws AccessDeniedException;

	Authenticator getAuthenticator(String resourceKey);

	Collection<PostAuthenticationStep> getPostAuthenticationSteps();

	int getAuthenticatorCount(Realm realm, String schemeResourceKey);
	
	Principal resolvePrincipalAndRealm(AuthenticationState state,
			String username, Realm selectedRealm, PrincipalType... types) throws AccessDeniedException,
			PrincipalNotFoundException;

	void setAuthenticationSchemeSelector(AuthenticationSchemeSelector authenticationSelector);

	void registerAuthenticatorSelector(AuthenticatorSelector selector);

	Authenticator nextAuthenticator(AuthenticationState state);

	<T> T callAs(Callable<T> callable, Principal principal);

	<T> T callAsSystemContext(Callable<T> callable, Realm realm)  throws Exception ;

	void runAsSystemContext(Runnable runnable, Realm realm);

}
