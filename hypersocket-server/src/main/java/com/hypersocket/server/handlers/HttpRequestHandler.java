/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hypersocket.server.HypersocketServer;

public abstract class HttpRequestHandler implements Comparable<HttpRequestHandler> {
	
	private int priority;
	private String name;
	protected HypersocketServer server;
	
	protected HttpRequestHandler(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}
	
	public abstract boolean handlesRequest(HttpServletRequest request);
	
	public abstract void handleHttpRequest(HttpServletRequest request, HttpServletResponse response, HttpResponseProcessor responseProcessor) throws IOException;
	
	public int getPriority() {
		return priority;
	}
	
	public void setServer(HypersocketServer server) {
		this.server = server;
		registered();
	}
	
	protected void registered() {
		
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(HttpRequestHandler obj) {
		return new Integer(this.getPriority()).compareTo(new Integer(obj.getPriority()));
	}
	
	
}
