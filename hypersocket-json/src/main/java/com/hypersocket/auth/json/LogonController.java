/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticationServiceImpl;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.FallbackAuthenticationRequired;
import com.hypersocket.json.AuthenticationRequiredResult;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.Session;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

@Controller
public class LogonController extends AuthenticatedController {

	
	@RequestMapping(value = "logon/reset", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult resetLogon(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Boolean redirect)
			throws AccessDeniedException, UnauthorizedException, IOException,
			RedirectException {
		AuthenticationState state = (AuthenticationState) request.getSession()
				.getAttribute(AUTHENTICATION_STATE_KEY);
		
		String previousScheme = (String) request.getSession().getAttribute(PREVIOUS_AUTHENTICATION_SCHEME);
		if(previousScheme==null) {
			previousScheme = state == null ? AuthenticationServiceImpl.BROWSER_AUTHENTICATION_RESOURCE_KEY
					: state.getScheme().getResourceKey();
		}
		return resetLogon(
				request,
				response,
				previousScheme, redirect);
	}

	@RequestMapping(value = "logon/reset/{scheme}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult resetLogon(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String scheme,
			@RequestParam(required = false) Boolean redirect)
			throws AccessDeniedException, UnauthorizedException, IOException,
			RedirectException {
		
		AuthenticationState currentState = 
				(AuthenticationState) request.getSession().getAttribute(AUTHENTICATION_STATE_KEY);
		
		if(currentState!=null) {
			request.getSession().setAttribute(PREVIOUS_AUTHENTICATION_SCHEME,currentState.getScheme().getResourceKey());
		}
		
		request.getSession().setAttribute(AUTHENTICATION_STATE_KEY, null);
		AuthenticationResult result = logon(request, response, scheme);
		if (Boolean.TRUE.equals(redirect)) {
			/**
			 * This resets back to zero so the logon UI does not show
			 * any error messages.
			 */
			AuthenticationState state = (AuthenticationState) request
					.getSession().getAttribute(AUTHENTICATION_STATE_KEY);
			state.clean();
			throw new RedirectException(System.getProperty(
					"hypersocket.uiPath", "/hypersocket/ui"));
		} else {
			return result;
		}

	}

	public AuthenticationState resetAuthenticationState(
			HttpServletRequest request, HttpServletResponse response,
			String scheme) throws AccessDeniedException, UnsupportedEncodingException {
		request.getSession().setAttribute(AUTHENTICATION_STATE_KEY, null);
		return createAuthenticationState(scheme, request, response);
	}

	@RequestMapping(value = "logon", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult logon(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, IOException, RedirectException {
		return logon(request, response, null);
	}

	@RequestMapping(value = "logon/{scheme}", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult logon(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String scheme)
			throws AccessDeniedException, UnauthorizedException, IOException, RedirectException {

		setupSystemContext(realmService.getRealmByHost(request.getServerName()));

		AuthenticationState state = (AuthenticationState) request
				.getSession().getAttribute(AUTHENTICATION_STATE_KEY);
		
		try {
			Session session;

			String flash = (String) request.getSession().getAttribute("flash");
			request.getSession().removeAttribute("flash");

			try {
				
				if(state==null) {
					session = sessionUtils.touchSession(request, response);
	
					if (session != null) {
						
						return getSuccessfulResult(session, flash, request, response);
					
					}
				}
			} catch (UnauthorizedException e) {
				// We are already in login so just continue
			} catch (SessionTimeoutException ex) {
				// Previous session has timed out
			}


			boolean createdState = false;
			if (state == null
					|| (!StringUtils.isEmpty(scheme) && !state.getScheme()
							.getResourceKey().equals(scheme))) {
				// We have not got login state so create
				state = createAuthenticationState(
						scheme == null ? AuthenticationServiceImpl.BROWSER_AUTHENTICATION_RESOURCE_KEY
								: scheme, request, response);
				createdState = true;
			}

			boolean success = false;
			
			if(request.getParameterMap().size() > 1 || !createdState ||  request.getHeader("Authorization") != null) {
				success = authenticationService.logon(state,
					decodeParameters(request.getParameterMap()));
			}
			
			if(state.getSession()!=null) {
				attachSession(state.getSession(), request, response);
			}
			
			if (state.isAuthenticationComplete()
					&& !state.hasPostAuthenticationStep()) {

				// We have authenticated!
				request.getSession().removeAttribute(AUTHENTICATION_STATE_KEY);

				setupAuthenticatedContext(state.getSession(), state.getLocale());
				
				try {
					return getSuccessfulResult(
							state.getSession(),
							flash,
							state.getHomePage(),
							request, 
							response);
				} finally {
					clearAuthenticatedContext();
				}
			} else {

				checkRedirect(request, response);
				
				return new AuthenticationRequiredResult(
						configurationService.getValue(state.getRealm(),
								"logon.banner"),
						state.getLastErrorMsg(),
						state.getLastErrorIsResourceKey(),
						!state.isAuthenticationComplete() ? authenticationService
								.nextAuthenticationTemplate(state,
										request.getParameterMap())
								: authenticationService
										.nextPostAuthenticationStep(state),
						configurationService.hasUserLocales(), state.isNew(),
						!state.hasNextStep(), success || state.isNew(),
						state.isAuthenticationComplete(),
						state.getScheme().getLastButtonResourceKey());
				
			}
		} catch(FallbackAuthenticationRequired e) {
			return resetLogon(request, response, "fallback", false);
		} catch(RedirectException e) {
			throw e;
		} catch(Throwable t) {
			
			if(log.isErrorEnabled()) {
				log.error("Error in authentication flow", t);
			}
			state.setLastErrorMsg(t.getMessage());
			state.setLastErrorIsResourceKey(false);
			
//			resetLogon(request, response, false);
			
			return new AuthenticationRequiredResult(
					configurationService.getValue(state.getRealm(),
							"logon.banner"),
					state.getLastErrorMsg(),
					state.getLastErrorIsResourceKey(),
					!state.isAuthenticationComplete() ? authenticationService
							.nextAuthenticationTemplate(state,
									request.getParameterMap())
							: authenticationService
									.nextPostAuthenticationStep(state),
					configurationService.hasUserLocales(), state.isNew(),
					!state.hasNextStep(), state.isNew(),
					state.isAuthenticationComplete(),
					state.getScheme().getLastButtonResourceKey());
		} finally {
			clearAuthenticatedContext();
		}
	}

	protected void checkRedirect(HttpServletRequest request, HttpServletResponse response) throws RedirectException, IOException {
		
		if(request.getParameter("redirectTo")!=null) {
			throw new RedirectException(request.getParameter("redirectTo"));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map decodeParameters(Map parameterMap) throws UnsupportedEncodingException {
		
		Map params = new HashMap();
		for(Object key : parameterMap.keySet()) {
			Object val = parameterMap.get(key);
			if(val instanceof String[]) {
				String[] arr = (String[])val;
				if(arr.length == 1) {
					params.put(key, arr[0]);
				} else {
					for(int i=0;i<arr.length;i++) {
						arr[i] = arr[i];
					}
					params.put(key, arr);
				}
			} else if(val instanceof String) {
				params.put(key, (String)val);
			}
		}
		return params;
	}

	@RequestMapping(value = "logoff")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void logoff(HttpServletRequest request, HttpServletResponse response)
			throws UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.touchSession(request, response);
			if (session != null && sessionService.isLoggedOn(session, false)) {
				sessionService.closeSession(session);
				request.getSession().removeAttribute(
						SessionUtils.AUTHENTICATED_SESSION);
			}
		} finally {
			clearAuthenticatedContext();
		}
	}

	private void attachSession(Session session, HttpServletRequest request, HttpServletResponse response) {
		
		request.getSession().setAttribute(
				SessionUtils.AUTHENTICATED_SESSION, session);

		sessionUtils.addAPISession(request, response,
				session);
	}
	
	@RequestMapping(value = "attach/{authCode}/{sessionId}")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void attachSession(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String authCode,
			@PathVariable String sessionId, @RequestParam String location)
			throws UnauthorizedException, SessionTimeoutException, RedirectException {

		Session session = sessionService.getSession(sessionId);
		Session authSession = sessionService.getSessionTokenResource(authCode, Session.class);
		
		if(!authSession.equals(session)) {
			throw new UnauthorizedException();
		}
		
		setupAuthenticatedContext(session,
				sessionUtils.getLocale(request));

		try {
			attachSession(authSession, request, response);
			
			if(StringUtils.isEmpty(location)) {
				throw new RedirectException(System.getProperty(
						"hypersocket.uiPath", "/hypersocket/ui"));
			} else {
				throw new RedirectException(location);
			}
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@RequestMapping(value = "logoff/{id}")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void logoffSession(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String id)
			throws UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Session session = sessionService.getSession(id);
			if (session != null && sessionService.isLoggedOn(session, false)) {
				sessionService.closeSession(session);
			}
		} finally {
			clearAuthenticatedContext();
		}
	}

	private AuthenticationResult getSuccessfulResult(Session session,
			String info, String homePage, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException, RedirectException {
		
		checkRedirect(request,response);
		return new AuthenticationSuccessResult(info,
				configurationService.hasUserLocales(), session, homePage);
		
	}

	private AuthenticationResult getSuccessfulResult(Session session,
			String info, HttpServletRequest request, HttpServletResponse response) throws IOException, RedirectException {
		
		checkRedirect(request,response);
		
		return new AuthenticationSuccessResult(info,
				configurationService.hasUserLocales(), session, "");
		
	}

}
