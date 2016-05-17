/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;

public abstract class ServletRequestHandler extends HttpRequestHandler {

	static Logger log = LoggerFactory.getLogger(ServletRequestHandler.class);
	
    protected Servlet servlet;
    
    public ServletRequestHandler(String name, Servlet servlet, int priority) {
    	super(name, priority);
    	this.servlet = servlet;
    }
    
	@Override
	public void handleHttpRequest(HttpServletRequest request,
			HttpServletResponse response, 
			HttpResponseProcessor responseProcessor) {
		try {
			servlet.service(request, response);	
			responseProcessor.sendResponse(request, response, false);
		} catch (Throwable e) {
			log.error("Servlet error", e);
			try {
				responseProcessor.send500(request, response);
			} catch (IOException ex) {
				log.error("IO error", ex);
			}
		} 
	}
}
