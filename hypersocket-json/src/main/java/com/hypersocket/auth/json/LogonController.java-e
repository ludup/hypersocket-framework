/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationServiceImpl;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.AuthenticationRequiredResult;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.Session;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

@Controller
public class LogonController extends AuthenticatedController {

	@RequestMapping(value = "touch", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult touch(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();

		try {
			return getSuccessfulResult(
					sessionUtils.touchSession(request, response), "");
		} finally {
			clearSystemContext();
		}
	}

	@RequestMapping(value = "peek", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult peek(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();

		try {
			try {
				return getSuccessfulResult(sessionUtils.getSession(request), "");
			} catch (UnauthorizedException e) {
				return new AuthenticationRequiredResult();
			}
		} finally {
			clearSystemContext();
		}
	}

	@RequestMapping(value = "logon/reset", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult resetLogon(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException {
		AuthenticationState state = (AuthenticationState) request.getSession().getAttribute(AUTHENTICATION_STATE_KEY);
		return resetLogon(request, response, state.getScheme().getName());
	}
	
	@RequestMapping(value = "logon/reset/{scheme}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult resetLogon(HttpServletRequest request,
			HttpServletResponse response,
		    @PathVariable String scheme) throws AccessDeniedException,
			UnauthorizedException {
		request.getSession().setAttribute(AUTHENTICATION_STATE_KEY, null);	
		return logon(request, response, scheme);
		
	}
	
	@RequestMapping(value = "logon", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult logon(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException {
		return logon(request, response, AuthenticationServiceImpl.DEFAULT_AUTHENTICATION_SCHEME);
	}
	
	@RequestMapping(value = "logon/{scheme}", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult logon(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String scheme) throws AccessDeniedException,
			UnauthorizedException {

		setupSystemContext();

		try {
			Session session;

			try {
				session = sessionUtils.touchSession(request, response);

				if (session != null) {
					return getSuccessfulResult(session, "");
				}
			} catch (UnauthorizedException e) {
				// We are already in login so just continue
			} catch (SessionTimeoutException ex) {
				// Previous session has timed out
			}

			AuthenticationState state = (AuthenticationState) request
					.getSession().getAttribute(AUTHENTICATION_STATE_KEY);

			if (state == null) {
				// We have not got login state so create
				state = createAuthenticationState(scheme, request, response);
			} else {
				state.setNewSession(false);
			}
			
			authenticationService.logon(state, request.getParameterMap());

			if (state.isAuthenticationComplete()
					&& !state.hasPostAuthenticationStep()) {

				// We have authenticated!
				request.getSession().removeAttribute(AUTHENTICATION_STATE_KEY);
				request.getSession().setAttribute(
						SessionUtils.AUTHENTICATED_SESSION, state.getSession());

				sessionUtils.addAPISession(request, response,
						state.getSession());

				return getSuccessfulResult(state.getSession(),"");
			} else {

				return new AuthenticationRequiredResult(
						configurationService.getValue("logon.banner"),
						state.getLastErrorMsg(),
						state.getLastErrorIsResourceKey(),
						!state.isAuthenticationComplete() ? authenticationService
								.nextAuthenticationTemplate(state,
										request.getParameterMap())
								: authenticationService
										.nextPostAuthenticationStep(state),
						i18nService.hasUserLocales(),
						state.isNew(),
						!state.hasNextStep());
			}
		} finally {
			clearSystemContext();

		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "switchRealm/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult switchRealm(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws UnauthorizedException, AccessDeniedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.getActiveSession(request);

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				throw new ResourceNotFoundException(AuthenticationService.RESOURCE_BUNDLE,
						"error.invalidRealm", id);
			}

			sessionService.switchRealm(session, realm);

			return getSuccessfulResult(session, I18N.getResource(
					sessionUtils.getLocale(request), AuthenticationService.RESOURCE_BUNDLE,
					"info.inRealm", realm.getName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "switchLanguage/{lang}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus switchLanguage(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("lang") String locale)
			throws UnauthorizedException, AccessDeniedException,
			ResourceNotFoundException {
		sessionUtils.setLocale(request, response, locale);
		return new RequestStatus();
	}

	private AuthenticationResult getSuccessfulResult(Session session,
			String info) {
		return new AuthenticationSuccessResult(info,
				i18nService.hasUserLocales(), session);
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

}
