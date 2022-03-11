/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AbstractResourceRepository;

public interface Authenticator {

	String getResourceKey();

	AuthenticatorResult authenticate(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException;

	@SuppressWarnings("rawtypes")
	FormTemplate createTemplate(AuthenticationState state, Map params) ;

	String getResourceBundle();

	boolean isSecretModule();

	boolean isIdentityModule();

	AuthenticationModuleType getType();

	String[] getAllowedSchemes();

	boolean isHidden();
	
	@JsonIgnore
	boolean isEnabled();
	
	@JsonIgnore
	AbstractResourceRepository<AuthenticationScheme> getRepository();
	
	boolean isPropertiesModule();

	boolean requiresUserInput(AuthenticationState state) throws AccessDeniedException;

	boolean canAuthenticate(Principal principal) throws AccessDeniedException;

	String getCredentialsResourceKey();
}
