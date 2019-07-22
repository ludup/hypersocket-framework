/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;

@Component
public class ChangePasswordAuthenticationStep implements PostAuthenticationStep {

	public static final String RESOURCE_KEY = "changePassword";
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	PermissionService permissionService;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerPostAuthenticationStep(this);
	}
	
	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		return AuthenticationServiceImpl.AUTHENTICATION_SCHEME_USER_LOGIN_RESOURCE_KEY.equals(state.getScheme().getResourceKey()) 
				&& realmService.requiresPasswordChange(state.getPrincipal(), state.getRealm());
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public AuthenticatorResult process(AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException {
		
		String password = AuthenticationUtils.getRequestParameter(parameters, ChangePasswordTemplate.PASSWORD_FIELD);
		String confirmPassword = AuthenticationUtils.getRequestParameter(parameters, ChangePasswordTemplate.CONFIRM_PASSWORD_FIELD);
		
		if(password==null || password.trim().equals("")) { 
			state.setLastErrorMsg("error.emptyPassword");
			state.setLastErrorIsResourceKey(true);
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}
		
		if(!password.equals(confirmPassword)) {
			state.setLastErrorMsg("error.passwordsMustMatch");
			state.setLastErrorIsResourceKey(true);
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}
		
		authenticationService.setupSystemContext(state.getPrincipal());
		
		try {
			doPasswordChange(state, password);
			
			return AuthenticatorResult.AUTHENTICATION_SUCCESS;
			
		} catch (ResourceException e) {
			state.setLastErrorMsg(e.getMessage());
			state.setLastErrorIsResourceKey(false);
			
			return AuthenticatorResult.AUTHENTICATION_FAILURE_DISPALY_ERROR;
		} finally {
			authenticationService.clearPrincipalContext();
		}
		
		
	}

	protected void doPasswordChange(AuthenticationState state, String password) throws AccessDeniedException, ResourceException {
		if(state.hasParameter("password")) {
			realmService.changePassword(state.getPrincipal(), state.getParameter("password"), password);
		} else {
			realmService.setPassword(state.getPrincipal(), password, 
					isForceChangeRequired(state), 
					isAdministrative(state));
		}
	}

	protected boolean isForceChangeRequired(AuthenticationState principal) {
		return false;
	}
	
	protected boolean isAdministrative(AuthenticationState principal) {
		return false;
	}
	
	@Override
	public FormTemplate createTemplate(AuthenticationState state) {
		return new ChangePasswordTemplate(state, "changePassword.text");
	}

	@Override
	public int getOrderPriority() {
		return 0;
	}

	@Override
	public boolean requiresUserInput(AuthenticationState state) {
		return true;
	}

	@Override
	public boolean requiresSession(AuthenticationState state) {
		return false;
	}

}
