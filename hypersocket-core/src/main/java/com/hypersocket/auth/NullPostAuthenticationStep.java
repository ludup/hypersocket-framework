package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;

public class NullPostAuthenticationStep implements PostAuthenticationStep {

	@Override
	public boolean requiresProcessing(AuthenticationState state) throws AccessDeniedException {
		return true;
	}

	@Override
	public int getOrderPriority() {
		return Integer.MIN_VALUE;
	}

	@Override
	public String getResourceKey() {
		return "null";
	}

	@Override
	public AuthenticatorResult process(AuthenticationState state, Map parameters) throws AccessDeniedException {
		return AuthenticatorResult.AUTHENTICATION_SUCCESS;
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state) throws AccessDeniedException {
		return null;
	}

	@Override
	public boolean requiresUserInput(AuthenticationState state) throws AccessDeniedException {
		return false;
	}

	@Override
	public boolean requiresSession(AuthenticationState state) throws AccessDeniedException {
		return false;
	}

}
