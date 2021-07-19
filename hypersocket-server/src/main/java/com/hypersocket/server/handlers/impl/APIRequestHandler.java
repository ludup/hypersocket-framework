/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.json.utils.HypersocketUtils;
import com.hypersocket.server.HypersocketServerImpl;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.session.json.SessionUtils;

public class APIRequestHandler extends ServletRequestHandler {

	private SessionUtils sessionUtils; 
	
	@Override
	public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response,
		HttpResponseProcessor responseProcessor) {
		if(Objects.isNull(sessionUtils)) {
			sessionUtils = ApplicationContextServiceImpl.getInstance().getBean(SessionUtils.class);
		}
		
		
		if(sessionUtils.isValidCORSRequest(request)) {
			
			/**
			 * Allow CORS to this realm. We must allow credentials as the
			 * API will be useless without them.
			 */
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
			
			Set<String> methods = new HashSet<>();
			methods.addAll(Arrays.asList("GET", "PUT", "POST", "DELETE", "OPTIONS"));
			String requestMethod = request.getHeader("Access-Control-Request-Method");
			if (StringUtils.isNotEmpty(requestMethod) && !methods.contains(requestMethod)) {
				methods.add(requestMethod);
			}
			
			response.addHeader("Access-Control-Allow-Methods", HypersocketUtils.csv(methods.toArray(new String[0])));
			
			String requestHeaders = request.getHeader("Access-Control-Request-Headers");
			if (StringUtils.isNotEmpty(requestHeaders)) {
				response.addHeader("Access-Control-Allow-Headers", requestHeaders);
			}
		}
		super.handleHttpRequest(request, response, responseProcessor);
		
	}

	public APIRequestHandler(Servlet servlet,
			int priority) {
		super("api", servlet, priority);
	}
	
	protected void registered() {
		server.addCompressablePath(server.resolvePath(server.getAttribute(
				HypersocketServerImpl.API_PATH, HypersocketServerImpl.API_PATH)));
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(
				server.resolvePath(server.getAttribute(
						HypersocketServerImpl.API_PATH,
						HypersocketServerImpl.API_PATH)));
	}

	@Override
	public boolean getDisableCache() {
		return false;
	}

}
