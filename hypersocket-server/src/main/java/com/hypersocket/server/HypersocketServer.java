/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;

public interface HypersocketServer {

	final static String RESOURCE_BUNDLE = "HypersocketServer";
	

	public abstract List<HttpRequestHandler> getHttpHandlers();

	public abstract List<WebsocketHandler> getWebsocketHandlers();
	
	public abstract void registerHttpHandler(HttpRequestHandler handler);

	public abstract void unregisterHttpHandler(HttpRequestHandler handler);

	public abstract void setAttribute(String name, Object value);

	public abstract <T> T getAttribute(String name, T template);

	public abstract String getApplicationName();

	public abstract void start() throws IOException;

	public abstract void stop();

	public abstract DispatcherServlet getDispatcherServlet();

	public abstract ServletConfig getServletConfig();

	public abstract String getApplicationPath();
	
	public String resolvePath(String path);
	
	public void init(ApplicationContext applicationContext) throws AccessDeniedException, ServletException, IOException;
	
	public void connect(TCPForwardingClientCallback callback);

	ApplicationContext getApplicationContext();

	void registerWebsocketpHandler(WebsocketHandler wsHandler);

	String getBasePath();

	abstract String getUserInterfacePath();

	boolean isCompressablePath(String uri);

	void addCompressablePath(String path);

	String getUiPath();

	String getApiPath();

	public void restart(Long delay);

	public void shutdown(Long delay);

	public boolean isStopping();

	String[] getSSLProtocols();

	String[] getSSLCiphers();

	int getActualHttpPort();

	int getActualHttpsPort();

	void registerControllerPackage(String controllerPackage);

}
