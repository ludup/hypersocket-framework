package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.json.input.FormTemplate;

public interface AuthenticationServiceListener {

	void modifyTemplate(AuthenticationState state, FormTemplate template, boolean authenticated);

	void postProcess(Authenticator authenticator, AuthenticatorResult result, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap);

	void preProcess(Authenticator authenticator, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap);

	void preProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap);

	void postProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticatorResult result,
			AuthenticationState state, @SuppressWarnings("rawtypes") Map parameterMap);
}
