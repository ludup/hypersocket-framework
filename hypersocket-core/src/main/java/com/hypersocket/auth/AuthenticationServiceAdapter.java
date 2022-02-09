package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.json.input.FormTemplate;

public class AuthenticationServiceAdapter implements AuthenticationServiceListener {

	@Override
	public void modifyTemplate(AuthenticationState state, FormTemplate template, boolean authenticated) {

	}

	@Override
	public void postProcess(Authenticator authenticator, AuthenticatorResult result, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap) {

	}

	@Override
	public void preProcess(Authenticator authenticator, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap) {

	}

	@Override
	public void preProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap) {

	}

	@Override
	public void postProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticatorResult result,
			AuthenticationState state, @SuppressWarnings("rawtypes") Map parameterMap) {

	}

}
