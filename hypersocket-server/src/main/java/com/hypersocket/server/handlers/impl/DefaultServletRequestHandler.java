package com.hypersocket.server.handlers.impl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.hypersocket.server.HypersocketServer;
import com.hypersocket.servlet.HypersocketServletConfig;

public class DefaultServletRequestHandler extends ServletRequestHandler {

	public DefaultServletRequestHandler(String path, Servlet servlet, int priority, HypersocketServer server)
			throws ServletException {
		super(path, servlet, priority);
	}

	protected void registered() {
		try {
			HypersocketServletConfig servletConfig = new HypersocketServletConfig(getName(),
					server.getServletContext());
			servlet.init(servletConfig);
		} catch (ServletException e) {
			log.error("Failed to init servlet", e);
		}
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(server.resolvePath(server.getAttribute(getName(), getName())));
	}

}
