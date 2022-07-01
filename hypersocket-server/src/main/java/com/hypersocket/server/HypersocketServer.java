/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;

public interface HypersocketServer {

	final static String RESOURCE_BUNDLE = "HypersocketServer";
	String CONTENT_INPUTSTREAM = "ContentInputStream";
	String BROWSER_URI = "browserRequestUri";

	List<HttpRequestHandler> getHttpHandlers();

	List<WebsocketHandler> getWebsocketHandlers();

	void registerHttpHandler(HttpRequestHandler handler);

	void unregisterHttpHandler(HttpRequestHandler handler);

	void setAttribute(String name, Object value);

	<T> T getAttribute(String name, T template);

	String getApplicationName();

	void start() throws IOException;

	void stop();

	DispatcherServlet getDispatcherServlet();

	String getApplicationPath();

	String resolvePath(String path);

	void init(ApplicationContext applicationContext) throws AccessDeniedException, ServletException, IOException;

	void connect(TCPForwardingClientCallback callback) throws IOException;

	ServletContext getServletContext();

	ApplicationContext getApplicationContext();

	void registerWebsocketpHandler(WebsocketHandler wsHandler);

	String getBasePath();

	abstract String getUserInterfacePath();

	boolean isCompressablePath(String uri);

	void addCompressablePath(String path);

	String getUiPath();

	String getApiPath();

	void restart(Long delay);

	void shutdown(Long delay);

	boolean isStopping();

	String[] getSSLProtocols();

	String[] getSSLCiphers();

	int getActualHttpPort();

	int getActualHttpsPort();

	void registerControllerPackage(String controllerPackage);

	void addUrlRewrite(String regex, String rewrite);

	Map<Pattern, String> getUrlRewrites();

	void addAlias(String alias, String path);

	boolean isRedirectable(String uri);

	void setRedirectable(String uri, boolean redirectable);

	Map<String, String> getAliases();

	void removeAlias(String alias);

	boolean isAliasFor(String redirectPage, String uri);

	ExecutorService getExecutor();

	void registerClientConnector(ClientConnector connector);

	void processDefaultResponse(HttpServletRequest request, HttpServletResponse response, boolean disableCache);

	String getDefaultRedirectPath(HttpServletRequest request, HttpServletResponse response);

	void setDefaultRedirectPath(String path);

	List<HomePageResolver> getHomePageResolvers();

	void addHomePageResolver(HomePageResolver homePageResolver);

	void removeHomePageResolver(HomePageResolver homePageResolver);

	String processReplacements(String str);

	void protectPage(String page);

	boolean isProtectedPage(String page);

	int getActiveCount();

	int getPoolSize();
	
	HttpServletRequest getCurrentRequest();

	void setContentStream(HttpServletRequest request, InputStream stream);
	
	void addLoggingOutputListener(LoggingOutputListener listener);
	
	void removeLoggingOutputListener(LoggingOutputListener listener);
}
