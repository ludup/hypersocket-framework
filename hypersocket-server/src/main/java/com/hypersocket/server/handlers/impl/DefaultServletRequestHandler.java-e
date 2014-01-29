package com.hypersocket.server.handlers.impl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.hypersocket.server.HypersocketServer;

public class DefaultServletRequestHandler extends ServletRequestHandler {

	HypersocketServer server;
	public DefaultServletRequestHandler(String path, Servlet servlet,
			int priority, HypersocketServer server) throws ServletException {
		super(path, servlet, priority);
		this.server = server;
		servlet.init(server.getServletConfig());
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(server.resolvePath(server.getAttribute(getName(), getName())));
	}

}
