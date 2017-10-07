/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import java.io.FileNotFoundException;
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
	
	public static final String LOCATION = "Location";

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

	synchronized AuthenticationState createAuthenticationState(String scheme,
			HttpServletRequest request, HttpServletResponse response,
			Realm realm, AuthenticationState mergeState)
			throws AccessDeniedException, UnsupportedEncodingException {
		
		if(realm==null) {
			realm = realmService.getRealmByHost(request.getServerName());
		}

		Map<String, Object> environment = new HashMap<String, Object>();
		for (BrowserEnvironment env : BrowserEnvironment.values()) {
			if (request.getHeader(env.toString()) != null) {
				environment.put(env.toString(),
						request.getHeader(env.toString()));
			}
		}
		
		String originalUri = (String)request.getAttribute("browserRequestUri");
		if(originalUri!=null) {
			environment.put("originalUri", originalUri);
		}
		environment.put("uri", request.getRequestURI());
		environment.put("url", request.getRequestURL().toString());

		AuthenticationState state = authenticationService
				.createAuthenticationState(scheme, request.getRemoteAddr(),
						environment, sessionUtils.getLocale(request), realm);
		List<AuthenticationModule> modules = state.getModules();
		for(AuthenticationModule module : modules) {
			if(authenticationService.getAuthenticator(module.getTemplate())==null) {
				
				state = createAuthenticationState("fallback", request, response, realm, mergeState);
				state.setLastErrorIsResourceKey(true);
				state.setLastErrorMsg("revertedFallback.adminOnly");
				return state;
			}
		}
		
		if(mergeState!=null) {
			state.getParameters().putAll(mergeState.getParameters());
			state.setLastErrorIsResourceKey(mergeState.getLastErrorIsResourceKey());
			state.setLastErrorMsg(mergeState.getLastErrorMsg());
		} else {
			Enumeration<?> names = request.getParameterNames();
			while(names.hasMoreElements()) {
				String name = (String) names.nextElement();
				state.addParameter(name, URLDecoder.decode(request.getParameter(name), "UTF-8"));
			}
		}
		
		request.getSession().setAttribute(AUTHENTICATION_STATE_KEY, state);
		return state;
	}

	@ExceptionHandler(RedirectException.class)
	@ResponseStatus(value = HttpStatus.FOUND)
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
	
	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public void fileNotFound(HttpServletRequest request,
			HttpServletResponse response, FileNotFoundException redirect) {

	}

	public SessionUtils getSessionUtils() {
		return sessionUtils;
	}

	protected Principal getSystemPrincipal() {
		return realmService.getSystemPrincipal();
	}
	
	protected void setupSystemContext(Realm realm) throws AccessDeniedException {
		authenticationService.setupSystemContext(realm);	
	}
	
	protected void setupSystemContext() throws AccessDeniedException {
		authenticationService.setupSystemContext(realmService.getSystemRealm());	
	}
	
	protected void setupAnonymousContext(String remoteAddress,
			String serverName, 
			String userAgent, 
			Map<String, String[]> parameters)
			throws AccessDeniedException {
		
		Realm realm = realmService.getRealmByHost(serverName);
		
		if(log.isDebugEnabled()) {
			log.debug("Logging anonymous onto the " + realm.getName() + " realm [" + serverName + "]");
		}
		
		setupAuthenticatedContext(sessionService.getSystemSession(), Locale.getDefault(), realm);

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

	protected void clearAuthenticatedContext() {
		authenticationService.clearPrincipalContext();
	}

	@ExceptionHandler(NumberFormatException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void handleException(HttpServletRequest request, NumberFormatException ex) {
		
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
