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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

public class AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(AuthenticatedController.class);

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
	
	@Autowired
	private PermissionService permissionService; 
	

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
	
	protected void setupSystemContext(Principal principal) {
		authenticationService.setupSystemContext(principal);
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
	
	protected <T> T callAsSystemContext(Callable<T> callable, Realm realm) throws Exception {
		return authenticationService.callAsSystemContext(callable, realm);
	}
	
	protected void setupAuthenticatedContext(Session session, Locale locale) {
		authenticationService.setCurrentSession(session, locale);
	}

	protected void setupAuthenticatedContext(Session session, Locale locale, Realm realm) throws AccessDeniedException {
		authenticationService.setCurrentSession(session, realm, locale);	
	}

	protected <T> T callAsRequestAuthenticatedContext(HttpServletRequest request, Callable<T> callable) throws Exception {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			return callable.call();
		} finally {
			clearAuthenticatedContext();
		}	
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
	
	protected void assertResourceAccess(AssignableResource resource, Principal principal) throws AccessDeniedException {
		permissionService.assertResourceAccess(resource, principal);
	}

	@ExceptionHandler(NumberFormatException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void handleException(HttpServletRequest request, NumberFormatException ex) {
		
	}
	
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleException(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
		// Log this?
		if (log.isErrorEnabled()) {
			log.error("Caught internal error", ex);
		}
		
		if(ex instanceof IllegalStateException) {
			ex = ex.getCause();
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);
		
		response.setContentType("text/html");
		
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
