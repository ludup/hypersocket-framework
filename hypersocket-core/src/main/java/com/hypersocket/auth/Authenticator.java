/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import com.hypersocket.resource.AbstractResourceRepository;

public interface Authenticator {

	public String getResourceKey();

	public AuthenticatorResult authenticate(AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException;

	@SuppressWarnings("rawtypes")
	public FormTemplate createTemplate(AuthenticationState state, Map params);

	public String getResourceBundle();

	boolean isSecretModule();

	boolean isIdentityModule();

	AuthenticationModuleType getType();

	String[] getAllowedSchemes();

	boolean isHidden();
	
	@JsonIgnore
	AbstractResourceRepository<AuthenticationScheme> getRepository();
	
	boolean isPropertiesModule();
}
