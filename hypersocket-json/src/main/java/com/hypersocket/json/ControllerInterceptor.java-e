/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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

@Component
public class ControllerInterceptor implements HandlerInterceptor {

	private static Logger log = LoggerFactory.getLogger(ControllerInterceptor.class);
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod) {
			
			HandlerMethod method = (HandlerMethod) handler;
			
			if(method.getMethodAnnotation(AuthenticationRequired.class)!=null
					|| method.getMethodAnnotation(AuthenticationRequiredButDontTouchSession.class)!=null) {
				
				if(!(method.getBean() instanceof AuthenticatedController)) {
					if(log.isErrorEnabled()) {
						log.error("Use of @AuthenticationRequired annotation is restricted to subclass of AuthenticatedController");
					}
					throw new IllegalArgumentException("Use of @AuthenticationRequired annotation is restricted to subclass of AuthenticatedController");
				}
				
				AuthenticatedController contrl = (AuthenticatedController) method.getBean();
				
				if(method.getMethodAnnotation(AuthenticationRequiredButDontTouchSession.class)!=null) {
					contrl.getSessionUtils().getSession(request);
				} else {
					contrl.getSessionUtils().touchSession(request, response);
				}
			}
		} 
		
		return true;
		
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		if(authenticationService.hasAuthenticatedContext() || authenticationService.hasSessionContext()) {
			if(log.isInfoEnabled()) {
				log.info(String.format("%s %s still has authenticated/session context. Will remove", request.getMethod(), request.getRequestURI()));
			}
			authenticationService.clearPrincipalContext();
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

}
