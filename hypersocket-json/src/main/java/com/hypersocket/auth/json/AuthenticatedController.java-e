/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.BrowserEnvironment;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

public class AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(AuthenticatedController.class);

	static final String AUTHENTICATION_STATE_KEY = "authenticationState";

	static final String LOCATION = "Location";

	@Autowired
	protected AuthenticationService authenticationService;

	@Autowired
	protected SessionService sessionService;

	@Autowired
	protected SessionUtils sessionUtils;

	@Autowired
	protected PermissionService permissionService;

	@Autowired
	protected RealmService realmService;

	@Autowired
	protected ConfigurationService configurationService;

	@Autowired
	protected I18NService i18nService;

	AuthenticationState createAuthenticationState(String scheme, 
			HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException {

		Map<String, Object> environment = new HashMap<String, Object>();
		for (BrowserEnvironment env : BrowserEnvironment.values()) {
			if (request.getHeader(env.toString()) != null) {
				environment.put(env.toString(),
						request.getHeader(env.toString()));
			}
		}
		
		AuthenticationState state = authenticationService
				.createAuthenticationState(scheme,
						request.getRemoteAddr(), environment,
						sessionUtils.getLocale(request));
		request.getSession().setAttribute(AUTHENTICATION_STATE_KEY, state);
		return state;
	}

	@ExceptionHandler(RedirectException.class)
	@ResponseStatus(value = HttpStatus.MOVED_TEMPORARILY)
	public void redirectToLogin(HttpServletRequest request,
			HttpServletResponse response, RedirectException redirect) {
		response.setHeader(LOCATION, redirect.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void unauthorizedAccess(HttpServletRequest request,
			HttpServletResponse response, UnauthorizedException redirect) {

	}

	@ExceptionHandler(SessionTimeoutException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void sessionTimeout(HttpServletRequest request,
			HttpServletResponse response, UnauthorizedException redirect) {

	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public void unauthorizedAccess(HttpServletRequest request,
			HttpServletResponse response, AccessDeniedException redirect) {

	}

	public SessionUtils getSessionUtils() {
		return sessionUtils;
	}

	protected Principal getSystemPrincipal() {
		return realmService.getSystemPrincipal();
	}

	protected void setupSystemContext() {
		setupAuthenticatedContext(getSystemPrincipal(),
				i18nService.getDefaultLocale(), getSystemPrincipal().getRealm());
	}

	protected void clearSystemContext() {
		clearAuthenticatedContext();
	}

	protected void setupAuthenticatedContext(Session pricipal, Locale locale,
			AuthenticatedService... services) {
		authenticationService.setCurrentSession(pricipal, locale);
		sessionService.setCurrentSession(pricipal, locale);
		realmService.setCurrentSession(pricipal, locale);
		configurationService.setCurrentSession(pricipal, locale);
		permissionService.setCurrentSession(pricipal, locale);
		for (AuthenticatedService service : services) {
			service.setCurrentSession(pricipal, locale);
		}
	}

	protected void setupAuthenticatedContext(Principal principal,
			Locale locale, Realm realm, AuthenticatedService... services) {
		authenticationService.setCurrentPrincipal(principal, locale, realm);
		sessionService.setCurrentPrincipal(principal, locale, realm);
		realmService.setCurrentPrincipal(principal, locale, realm);
		configurationService.setCurrentPrincipal(principal, locale, realm);
		permissionService.setCurrentPrincipal(principal, locale, realm);
		for (AuthenticatedService service : services) {
			service.setCurrentPrincipal(principal, locale, realm);
		}
	}

	protected void clearAuthenticatedContext(AuthenticatedService... services) {

		authenticationService.clearPrincipalContext();
		sessionService.clearPrincipalContext();
		realmService.clearPrincipalContext();
		configurationService.clearPrincipalContext();
		for (AuthenticatedService service : services) {
			service.clearPrincipalContext();
		}
	}

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleException(Throwable ex) {
		// Log this?
		if (log.isErrorEnabled()) {
			log.error("Caught internal error", ex);
		}
	}

}
