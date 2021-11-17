package com.hypersocket.server.handlers.impl;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.servlet.HypersocketServletConfig;
import com.hypersocket.session.json.SessionUtils;

public class H2ConsoleServlet extends ServletRequestHandler {

	private SessionUtils sessionUtils;
	private PermissionService permissionService;
	
	public H2ConsoleServlet() {
		super("console", new org.h2.server.web.WebServlet(), Integer.MIN_VALUE);
	}
	
	protected void registered() {
		try {
			
			sessionUtils = ApplicationContextServiceImpl.getInstance().getBean(SessionUtils.class);
			permissionService = ApplicationContextServiceImpl.getInstance().getBean(PermissionService.class);
			SystemConfigurationService systemConfigurationService = ApplicationContextServiceImpl.getInstance().getBean(SystemConfigurationService.class);
			
			server.getServletContext().setInitParameter("db.user", systemConfigurationService.getValue("jdbc.username"));
			server.getServletContext().setInitParameter("db.password", systemConfigurationService.getValue("jdbc.password"));
			
			server.getServletContext().setInitParameter("db.url", "jdbc:h2:" + systemConfigurationService.getValue("jdbc.database"));
			//server.getServletContext().setInitParameter("db.url", "jdbc:h2:./h2/data");
			
			HypersocketServletConfig servletConfig = new HypersocketServletConfig(getName(), server.getServletContext());
			servletConfig.setInitParameter("webAllowOthers", "");
			servletConfig.setInitParameter("trace", "");
			servlet.init(servletConfig);
		} catch (ServletException e) {
			log.error("Failed to init servlet", e);
		}
	}
	
	@PostConstruct
	private void postConstruct() {
		server.registerHttpHandler(this);
	}

	
	@Override
	public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response,
			HttpResponseProcessor responseProcessor) {
		
		try {
			if(sessionUtils.hasActiveSession(request)) {
				permissionService.setCurrentSession(sessionUtils.getActiveSession(request), sessionUtils.getLocale(request));
				try {
					if(permissionService.hasSystemPermission(sessionUtils.getPrincipal(request))) {
						super.handleHttpRequest(request, response, responseProcessor);
						return;
					}
				} finally {
					permissionService.clearPrincipalContext();
				}
			}
		} catch (UnauthorizedException e) {
		}
		
		try {
			responseProcessor.send404(request, response);
			responseProcessor.sendResponse(request, response);
		} catch (IOException e) {
			log.error("Failed to send response", e);
		}
	}
	
	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(String.format("/%s", getName()));
	}

	@Override
	public boolean getDisableCache() {
		return true;
	}

}
