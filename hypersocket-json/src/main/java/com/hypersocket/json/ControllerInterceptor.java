/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.AuthenticationRequiredButDontTouchSession;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.json.SessionUtils;

@Component
public class ControllerInterceptor implements HandlerInterceptor {

	private final static Logger log = LoggerFactory.getLogger(ControllerInterceptor.class);

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private SessionUtils sessionUtils;
	@Autowired
	private RealmService realmService;

	@SuppressWarnings({ "deprecation", "removal" })
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {

			var method = (HandlerMethod) handler;

			if (method.getMethodAnnotation(AuthenticationRequired.class) != null
					|| method.getMethodAnnotation(AuthenticationRequiredButDontTouchSession.class) != null) {

				checkMethod(method);

				var contrl = (AuthenticatedController) method.getBean();
				if (method.getMethodAnnotation(AuthenticationRequiredButDontTouchSession.class) != null) {
					contrl.getSessionUtils().getSession(request);
				} else {
					contrl.getSessionUtils().touchSession(request, response);
				}
			}

			var acAnnotation = method.getMethodAnnotation(AuthenticatedContext.class);
			if (acAnnotation != null) {
				checkMethod(method);

				var contrl = (AuthenticatedController) method.getBean();
				if (acAnnotation.preferActive() && sessionUtils.hasActiveSession(request)) {
					if (acAnnotation.system())
						contrl.setupSystemContext(sessionUtils.getActiveSession(request).getCurrentRealm());
					else
						contrl.setCurrentSession(sessionUtils.getActiveSession(request),
								sessionUtils.getLocale(request));
				} else if (acAnnotation.currentRealmOrDefault()) {
					contrl.setupSystemContext(sessionUtils.getCurrentRealmOrDefault(request));
				} else if (acAnnotation.system()) {
					contrl.setupSystemContext();
				} else if (acAnnotation.realmHost()) {
					contrl.setupSystemContext(realmService.getRealmByHost(request.getServerName()));
				} else {
					contrl.setCurrentSession(sessionUtils.getSession(request), sessionUtils.getCurrentRealm(request),
							sessionUtils.getPrincipal(request), sessionUtils.getLocale(request));
				}
			}
			
			var was = Thread.currentThread().getContextClassLoader();
			if(was != null)
				request.setAttribute("lastContextClassLoader", was);
			Thread.currentThread().setContextClassLoader(method.getBeanType().getClassLoader());
		}

		return true;

	}

	protected void checkMethod(HandlerMethod method) {
		if (!(method.getBean() instanceof AuthenticatedController)) {
			if (log.isErrorEnabled()) {
				log.error(
						"Use of @AuthenticationRequired annotation is restricted to subclass of AuthenticatedController");
			}
			throw new IllegalArgumentException(
					"Use of @AuthenticationRequired annotation is restricted to subclass of AuthenticatedController");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		var clearContext = false;
		try {
			if (handler instanceof HandlerMethod) {
				HandlerMethod method = (HandlerMethod) handler;
				clearContext = method.getMethodAnnotation(AuthenticatedContext.class) != null;
			}
	
			if (clearContext) {
				if (authenticationService.hasAuthenticatedContext() || authenticationService.hasSessionContext()) {
					authenticationService.clearPrincipalContext();
				} else {
					if (log.isInfoEnabled()) {
						log.info(
								"{} {} was expecting to have a context to clear, but there was none. This suggests a coding error.",
								request.getMethod(), request.getRequestURI());
					}
				}
			} else if (authenticationService.hasAuthenticatedContext() || authenticationService.hasSessionContext()) {
				log.warn("{} {} still has authenticated/session context. Will remove", request.getMethod(),
						request.getRequestURI());
				authenticationService.clearPrincipalContext();
			}
		}
		finally {
			var was = (ClassLoader)request.getAttribute("lastContextClassLoader");
			if(was != null)
				Thread.currentThread().setContextClassLoader(was);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

}
