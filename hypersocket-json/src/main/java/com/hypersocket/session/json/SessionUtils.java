/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session.json;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionResourceToken;
import com.hypersocket.session.SessionService;

@Component
public class SessionUtils {

	static Logger log = LoggerFactory.getLogger(SessionUtils.class);

	public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	public static final String HYPERSOCKET_API_SESSION = "HYPERSOCKET_API_SESSION";
	public static final String HYPERSOCKET_CSRF_TOKEN = "HYPERSOCKET_CSRF_TOKEN";
	public static final String HYPERSOCKET_API_KEY = "apikey";
	public static final String USER_LOCALE = "userLocale";
	public static final String HYPERSOCKET_LOCALE = "HYPERSOCKET_LOCALE";

	@Autowired
	SessionService sessionService;

	@Autowired
	ConfigurationService configurationService; 
	
	@Autowired
	SystemConfigurationService systemConfigurationService; 
	
	public Session getActiveSession(HttpServletRequest request) {
		
		Session session = null;
		
		if(request.getParameterMap().containsKey(HYPERSOCKET_API_KEY)) {
			session = sessionService.getSession(request.getParameter(HYPERSOCKET_API_KEY));
		} else if(request.getHeader(HYPERSOCKET_API_SESSION) != null) {
			session = sessionService.getSession((String)request.getHeader(HYPERSOCKET_API_SESSION));
		}
		
		if (session != null && sessionService.isLoggedOn(session, true)) {
			return session;
		}
		
		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, true)) {
				return session;
			}
		}
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, true)) {
				return session;
			}
		}
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals(HYPERSOCKET_API_SESSION)) {
				session = sessionService.getSession(c.getValue());
				if (session != null && sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}
		}

		return null;
	}

	public Realm getCurrentRealm(HttpServletRequest request)
			throws UnauthorizedException {
		Session session = getActiveSession(request);
		if (session == null)
			throw new UnauthorizedException();
		return session.getCurrentRealm();
	}

	public Principal getPrincipal(HttpServletRequest request)
			throws UnauthorizedException {
		Session session = getActiveSession(request);
		if (session == null)
			throw new UnauthorizedException();
		return session.getCurrentPrincipal();
	}

	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response) throws UnauthorizedException,
			SessionTimeoutException, AccessDeniedException {
		return touchSession(request, response, true);
	}
	
	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response, boolean performCsrfCheck) throws UnauthorizedException,
			SessionTimeoutException, AccessDeniedException {

		Session session = null;
		
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) == null) {
			if (log.isDebugEnabled()) {
				log.debug("Session object not attached to HTTP session");
			}
			session = getActiveSession(request);
			if (session == null) {
				if (log.isDebugEnabled()) {
					log.debug("No session attached to request");
				}
				throw new UnauthorizedException();
			}
			if (!sessionService.isLoggedOn(session, true)) {
				throw new SessionTimeoutException();
			}
		} else {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if (!sessionService.isLoggedOn(session, true)) {
				throw new UnauthorizedException();
			}
		}

		if(performCsrfCheck) {
			verifySameSiteRequest(request, session);
		}
		// Preserve the session for future lookups in this request and session
		request.setAttribute(AUTHENTICATED_SESSION, session);
		request.getSession().setAttribute(AUTHENTICATED_SESSION, session);

		addAPISession(request, response, session);

		return session;

	}

	public Session getSession(HttpServletRequest request)
			throws UnauthorizedException, SessionTimeoutException {
		
		Session session = null;
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) == null) {
			if (log.isDebugEnabled()) {
				log.debug("Session object not attached to HTTP session");
			}
			session = getActiveSession(request);
			if (session == null) {
				if (log.isDebugEnabled()) {
					log.debug("No session attached to request");
				}
				throw new UnauthorizedException();
			}
			if (!sessionService.isLoggedOn(session, false)) {
				throw new SessionTimeoutException();
			}
		} else {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if (!sessionService.isLoggedOn(session, false)) {
				throw new UnauthorizedException();
			}
		}

		return session;

	}
	
	private void verifySameSiteRequest(HttpServletRequest request, Session session) throws AccessDeniedException, UnauthorizedException {
		
		if(!systemConfigurationService.getBooleanValue("security.enableCSRFProtection")) {
			return;
		}

		String requestToken = request.getHeader("X-Csrf-Token");
		if(requestToken==null) {
			requestToken = request.getParameter("token");
			if(requestToken==null) {
				log.warn(String.format("CSRF token missing from %s", request.getRemoteAddr()));
				throw new AccessDeniedException("CSRF token missing");
			}
		}
		
		if(!session.getCsrfToken().equals(requestToken)) {
			log.warn(String.format("CSRF token mistmatch from %s", request.getRemoteAddr()));
			throw new UnauthorizedException();
		}

	}

	public void addAPISession(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		Cookie cookie = new Cookie(HYPERSOCKET_API_SESSION, session.getId());
		cookie.setMaxAge(60 * session.getTimeout());
		if(request.getProtocol().equalsIgnoreCase("https")) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		cookie.setPath("/");
		response.addCookie(cookie);
		
		cookie = new Cookie(HYPERSOCKET_CSRF_TOKEN, session.getCsrfToken());
		cookie.setMaxAge(60 * session.getTimeout());
		cookie.setSecure(request.getProtocol().equalsIgnoreCase("https"));
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public Locale getLocale(HttpServletRequest request) {

		if (request.getSession().getAttribute(USER_LOCALE) == null) {

			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals(HYPERSOCKET_LOCALE)) {
					return new Locale(c.getValue());
				}
			}
			return Locale.getDefault();
		} else {
			return new Locale((String) request.getSession().getAttribute(
					USER_LOCALE));
		}

	}

	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, String locale) {

		request.getSession().setAttribute(USER_LOCALE, locale);

		Cookie cookie = new Cookie(HYPERSOCKET_LOCALE, locale);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		if(request.getProtocol().equalsIgnoreCase("https")) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		cookie.setDomain(request.getServerName());
		response.addCookie(cookie);

	}

	public void touchSession(Session session) throws SessionTimeoutException {

		if (!sessionService.isLoggedOn(session, true)) {
			throw new SessionTimeoutException();
		}
	}

	public <T> SessionResourceToken<T> authenticateSessionToken(
			HttpServletRequest request, HttpServletResponse response, String shortCode, Class<T> resourceClz)
			throws UnauthorizedException, SessionTimeoutException {

		SessionResourceToken<T> token = sessionService.getSessionToken(
				shortCode, resourceClz);

		if(token!=null) {

			if (!request.getRemoteAddr().equals(
					token.getSession().getRemoteAddress())) {
				if(log.isInfoEnabled()) {
					log.info(String.format("Session token %s for %s does not belong to %s", 
							shortCode, 
							request.getRemoteAddr(),
							token.getSession().getRemoteAddress()));
				}
				throw new UnauthorizedException();
			}
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Session token %s is valid", shortCode));
			}
			
			// Preserve the session for future lookups in this request and session
			request.setAttribute(AUTHENTICATED_SESSION, token.getSession());
			request.getSession().setAttribute(AUTHENTICATED_SESSION, token.getSession());

			addAPISession(request, response, token.getSession());
			
			return token;
			
		}

		if(log.isInfoEnabled()) {
			log.info(String.format("Session token %s is invalid", shortCode));
		}
		throw new UnauthorizedException();
	}

	public boolean hasActiveSession(HttpServletRequest request) {
		return getActiveSession(request)!=null;
	}

}
