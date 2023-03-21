/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session.json;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionResourceToken;
import com.hypersocket.session.SessionService;

@Component
public class SessionUtils {

	public static String COOKIE_SAME_SITE_DEFAULT = "Lax";
	static Logger log = LoggerFactory.getLogger(SessionUtils.class);
	
	static boolean debugCSRF = "true".equals(System.getProperty("hypersocket.csrfDebugRequests"));

	public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	public static final String HYPERSOCKET_API_SESSION = "HYPERSOCKET_API_SESSION";
	public static final String HYPERSOCKET_CSRF_TOKEN = "HYPERSOCKET_CSRF_TOKEN";
	public static final String HYPERSOCKET_API_KEY = "apikey";
	public static final String USER_LOCALE = "userLocale";
	public static final String HYPERSOCKET_LOCALE = "HYPERSOCKET_LOCALE";
	public static final String HYPERSOCKET_REBUILD_I18N = "rebuildI18N";

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ConfigurationService configurationService; 
	
	@Autowired
	private SystemConfigurationService systemConfigurationService; 
	
	@Autowired
	private RealmService realmService; 
	
	
	public Session getActiveSession(HttpServletRequest request) {
		
		Session session = null;
		
		if(request.getParameterMap().containsKey(HYPERSOCKET_API_KEY)) {
			session = sessionService.getSession(request.getParameter(HYPERSOCKET_API_KEY));
		} else if(request.getHeader(HYPERSOCKET_API_SESSION) != null) {
			session = sessionService.getSession((String)request.getHeader(HYPERSOCKET_API_SESSION));
		}
		
		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}
		
		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		if (Objects.nonNull(request.getSession()) && request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals(HYPERSOCKET_API_SESSION)) {
				session = sessionService.getSession(c.getValue());
				if (session != null && sessionService.isLoggedOn(session, false)) {
					return session;
				}
			}
		}

		return null;
	}

	/**
	 * Use AuthenticatedService.getCurrentPrincipal instead ensuring that a valid
	 * principal context has been setup previously.
	 */
	public Realm getCurrentRealm(HttpServletRequest request)
			throws UnauthorizedException {
		Session session = getActiveSession(request);
		if (session == null)
			throw new UnauthorizedException();
		return session.getCurrentRealm();
	}
	
	/**
	 * Use AuthenticatedService.getCurrentPrincipal instead ensuring that a valid
	 * principal context has been setup previously.
	 */
	public Realm getCurrentRealmOrDefault(HttpServletRequest request) {
		Session session = getActiveSession(request);
		if (session == null)
			return realmService.getRealmByHost(request.getServerName());
		return session.getCurrentRealm();
	}

	/**
	 * Use AuthenticatedService.getCurrentPrincipal instead ensuring that a valid
	 * principal context has been setup previously.
	 */
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

		/**
		 * This method SHOULD NOT touch the session.
		 */
		Session session = null;
		
		if(request.getParameterMap().containsKey(HYPERSOCKET_API_KEY)) {
			session = sessionService.getSession(request.getParameter(HYPERSOCKET_API_KEY));
		} else if(request.getHeader(HYPERSOCKET_API_SESSION) != null) {
			session = sessionService.getSession((String)request.getHeader(HYPERSOCKET_API_SESSION));
		}
		
		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}
		
		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals(HYPERSOCKET_API_SESSION)) {
				session = sessionService.getSession(c.getValue());
				if (session != null && sessionService.isLoggedOn(session, false)) {
					return session;
				}
			}
		}

		throw new UnauthorizedException();
	}
	
	private void verifySameSiteRequest(HttpServletRequest request, Session session) throws AccessDeniedException, UnauthorizedException {
		

		if(isValidCORSRequest(request)) {
			return;
		}
		
		if(!systemConfigurationService.getBooleanValue("security.enableCSRFProtection")) {
			return;
		}
		
		String requestToken = request.getHeader("X-Csrf-Token");
		if(requestToken==null) {
			requestToken = request.getParameter("token");
			if(requestToken==null) {
				log.warn(String.format("CSRF token missing from %s", request.getRemoteAddr()));
				debugRequest(request);
				throw new UnauthorizedException();
			}
		}
		
		if(!session.getCsrfToken().equals(requestToken)) {
			log.warn(String.format("CSRF token mistmatch from %s", request.getRemoteAddr()));
			debugRequest(request);
			throw new UnauthorizedException();
		}

	}

	protected void debugRequest(HttpServletRequest request) {
		if(debugCSRF) {
			log.warn(String.format("The request URI was %s, and contained the following parameters :-",request.getRequestURI()));
			for(Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				log.warn(String.format("  %s = %s", en.getKey(), String.join(",", en.getValue())));
			}
			log.warn("And the following headers :-");
			for(Enumeration<String> hdrEnum = request.getHeaderNames(); hdrEnum.hasMoreElements(); ) {
				String hdr = hdrEnum.nextElement();
				log.warn(String.format("  %s = %s", hdr, request.getHeader(hdr)));
			}
		}
	}

	public boolean isValidCORSRequest(HttpServletRequest request) {
		
		Realm currentRealm = getCurrentRealmOrDefault(request);
		String requestOrigin = request.getHeader("Origin");
		
		if(!Objects.isNull(requestOrigin)) {
			
			if(log.isDebugEnabled()) {
				log.debug("CORS request for origin {}", requestOrigin);
			}
			
			Set<String> origins = new HashSet<>();
			/**
			 * We hard code our own extensions to avoid the user having 
			 * to configure CORS or disable it.
			 */
			origins.add("chrome-extension://nbdlpjacpjcngebcjapombjkmjbjnpbc");
			origins.add("moz-extension://93d8c2f2-e17c-7d4b-9177-a45d2650c23b");
			
			if(requestOrigin.startsWith("moz-extension://")) {
				return true;
			}
			
			if(configurationService.getBooleanValue(currentRealm, "cors.enabled")) {
				
				origins.addAll(ResourceUtils.explodeCollectionValues(
						configurationService.getValue(currentRealm, "cors.origins")));
				
			}

			if(origins.contains(requestOrigin)) {
				if(log.isDebugEnabled()) {
					log.debug("CORS request SUCCEEDED for origin {}", requestOrigin);
				}
				return true;
			}
			
			if(log.isDebugEnabled()) {
				log.debug("CORS request FAILED origin {}", requestOrigin);
			}
		}
		
		return false;
	}
	
	public boolean isValidWebsocketRequest(HttpServletRequest request) {
		
		Realm currentRealm = getCurrentRealmOrDefault(request);
		String requestOrigin = request.getHeader("Origin");
		
		if (Objects.isNull(requestOrigin)) {
			return false;
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Websocket request for origin {}", requestOrigin);
		}
		
		
		if(configurationService.getBooleanValue(currentRealm, "websocket.enabled")) {
		
			Set<String> origins = new HashSet<>();
			
			origins.addAll(ResourceUtils.explodeCollectionValues(
					configurationService.getValue(currentRealm, "websocket.origins")));
			
			return origins.contains(requestOrigin);
			
		}
		
		
		return true;
	}
	
	public int calccSessionTimeoutSeconds(Session session) {
		return (session.getTimeout() > 0 ? 60 * session.getTimeout() : Integer.MAX_VALUE);
	}
	
	public void addAPISession(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		Cookie cookie = new Cookie(HYPERSOCKET_API_SESSION, session.getId());
		cookie.setMaxAge(calccSessionTimeoutSeconds(session));
		cookie.setSecure(request.getProtocol().equalsIgnoreCase("https"));
		cookie.setHttpOnly(true);
		cookie.setDomain(request.getServerName());
		cookie.setPath("/");
		var cfg = System.getProperty("hypersocket.cookie.sameSite", SessionUtils.COOKIE_SAME_SITE_DEFAULT);
		if(!cfg.equalsIgnoreCase("Omit")) {
			cookie.setComment("; SameSite=" + cfg);
		}
		response.addCookie(cookie);
		
		cookie = new Cookie(HYPERSOCKET_CSRF_TOKEN, session.getCsrfToken());
		cookie.setMaxAge(calccSessionTimeoutSeconds(session));
		cookie.setSecure(request.getProtocol().equalsIgnoreCase("https"));
		cookie.setPath("/");
		cookie.setHttpOnly(false); // hypersocket-utils.js#getCsrfToken()
		cookie.setDomain(request.getServerName());
		if(!cfg.equalsIgnoreCase("Omit")) {
			cookie.setComment("; SameSite=" + cfg);
		}
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
			try {
				return configurationService.callAsSystemContext(() -> configurationService.getDefaultLocale());
			} catch (Exception e) {
				throw new IllegalStateException("Failed to get default locale.", e);
			}
		} else {
			return new Locale((String) request.getSession().getAttribute(
					USER_LOCALE));
		}

	}

	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, String locale) {

		request.getSession().setAttribute(USER_LOCALE, locale);
		request.getSession().setAttribute(HYPERSOCKET_REBUILD_I18N, true);

		Cookie cookie = new Cookie(HYPERSOCKET_LOCALE, locale);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");

		cookie.setSecure(request.getProtocol().equalsIgnoreCase("https"));
		cookie.setHttpOnly(true);
		cookie.setDomain(request.getServerName());
		var cfg = System.getProperty("hypersocket.cookie.sameSite", SessionUtils.COOKIE_SAME_SITE_DEFAULT);
		if(!cfg.equalsIgnoreCase("Omit")) {
			cookie.setComment("; SameSite=" + cfg);
		}
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
		try {
			return getSession(request)!=null;
		} catch (UnauthorizedException | SessionTimeoutException e) {
			return false;
		}
	}

}
