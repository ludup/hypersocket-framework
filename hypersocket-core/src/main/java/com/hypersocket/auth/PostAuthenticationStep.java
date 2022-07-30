/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;

public interface PostAuthenticationStep {

	boolean requiresProcessing(AuthenticationState state) throws AccessDeniedException;
	
	int getOrderPriority();
	
	String getResourceKey();
	
	AuthenticatorResult process(AuthenticationState state, Map<String, String[]> parameters) throws AccessDeniedException;
	
	FormTemplate createTemplate(AuthenticationState state) throws AccessDeniedException;

	boolean requiresUserInput(AuthenticationState state) throws AccessDeniedException;

	boolean requiresSession(AuthenticationState state) throws AccessDeniedException;
}
