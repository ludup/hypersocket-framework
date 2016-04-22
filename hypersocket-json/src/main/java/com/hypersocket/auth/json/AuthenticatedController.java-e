/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.BrowserEnvironment;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

public class AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(AuthenticatedController.class);

	public static final String AUTHENTICATION_STATE_KEY = "authenticationState";
	public static final String PREVIOUS_AUTHENTICATION_SCHEME = "previousAuthScheme";
	
	static final String LOCATION = "Location";

	@Autowired
	protected AuthenticationService authenticationService;

	@Autowired
	protected SessionService sessionService;

	@Autowired
	protected SessionUtils sessionUtils;

	@Autowired
	protected PermissionRepository permissionRepository;

	@Autowired
	protected RealmService realmService;

	@Autowired
	protected ConfigurationService configurationService;

	@Autowired
	protected I18NService i18nService;

	AuthenticationState createAuthenticationState(String scheme,
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnsupportedEncodingException {

		Map<String, Object> environment = new HashMap<String, Object>();
		for (BrowserEnvironment env : BrowserEnvironment.values()) {
			if (request.getHeader(env.toString()) != null) {
				environment.put(env.toString(),
						request.getHeader(env.toString()));
			}
		}

		AuthenticationState state = authenticationService
				.createAuthenticationState(scheme, request.getRemoteAddr(),
						environment, sessionUtils.getLocale(request));
		List<AuthenticationModule> modules = state.getModules();
		for(AuthenticationModule module : modules) {
			if(authenticationService.getAuthenticator(module.getTemplate())==null) {
				
				state = createAuthenticationState("fallback", request, response);
				state.setLastErrorIsResourceKey(true);
				state.setLastErrorMsg("revertedFallback.adminOnly");
				return state;
			}
		}
		Enumeration<?> names = request.getParameterNames();
		while(names.hasMoreElements()) {
			String name = (String) names.nextElement();
			state.addParameter(name, URLDecoder.decode(request.getParameter(name), "UTF-8"));
		}
		
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
	
	protected void setupSystemContext(Realm realm) throws AccessDeniedException {
		setupAuthenticatedContext(sessionService.getSystemSession(),
				Locale.getDefault(), realm);
	}
	
	protected void setupAnonymousContext(String remoteAddress,
			String serverName, 
			String userAgent, 
			Map<String, String> parameters)
			throws AccessDeniedException {
		
		Realm realm = realmService.getRealmByHost(serverName);
		
		if(log.isInfoEnabled()) {
			log.info("Logging anonymous onto the " + realm.getName() + " realm [" + serverName + "]");
		}
		
		setupAuthenticatedContext(sessionService.getSystemSession(), Locale.getDefault());

	}

	protected void clearAnonymousContext() {
		clearAuthenticatedContext();
	}
	
	protected void setupAuthenticatedContext(Session session, Locale locale) {
		authenticationService.setCurrentSession(session, locale);
	}

	protected void setupAuthenticatedContext(Session session, Locale locale, Realm realm) throws AccessDeniedException {
		authenticationService.setCurrentSession(session, realm, locale);	
	}
	
	protected boolean hasSessionContext() {
		return authenticationService.hasSessionContext();
	}
	
	protected Realm getCurrentRealm() {
		return authenticationService.getCurrentRealm();
	}
	
	protected Principal getCurrentPrincipal() {
		return authenticationService.getCurrentPrincipal();
	}
	
	protected Session getCurrentSession() {
		return authenticationService.getCurrentSession();
	}

//	protected void setupAuthenticatedContext(Principal principal,
//			Locale locale, Realm realm) {
//		authenticationService.setCurrentPrincipal(principal, locale, realm);
//	}

	protected void clearAuthenticatedContext() {
		authenticationService.clearPrincipalContext();
	}

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleException(HttpServletRequest request, Throwable ex) {
		// Log this?
		if (log.isErrorEnabled()) {
			log.error("Caught internal error", ex);
		}
		
		if(ex instanceof IllegalStateException) {
			ex = ex.getCause();
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);
		
		try {
		ex.printStackTrace(pwriter);
		request.setAttribute("message", ex.getMessage());
		request.setAttribute("stacktrace", writer.toString());
		
		} finally {
			pwriter.close();
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
		
	}

}
