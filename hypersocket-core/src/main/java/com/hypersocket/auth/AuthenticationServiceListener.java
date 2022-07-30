package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.json.input.FormTemplate;

public interface AuthenticationServiceListener {

	default void modifyTemplate(AuthenticationState state, FormTemplate template, boolean authenticated) {
	}

	default void postProcess(Authenticator authenticator, AuthenticatorResult result, AuthenticationState state,
			Map<String, String[]> parameterMap) {
	}

	default void preProcess(Authenticator authenticator, AuthenticationState state,
			Map<String, String[]> parameterMap) {
	}

	default void preProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticationState state,
			Map<String, String[]> parameterMap) {
	}

	default void postProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticatorResult result,
			AuthenticationState state, Map<String, String[]> parameterMap) {
	}
}
