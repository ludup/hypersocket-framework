/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

public class HypersocketServletContext implements ServletContext {

	HypersocketServletConfig config;
	String contextPath;
	Map<String,Object> attributes = new HashMap<String,Object>();
	static Logger log = LoggerFactory.getLogger(HypersocketServletContext.class);
	ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
	
	HypersocketServletContext(HypersocketServletConfig config, String contextPath) {
		this.config = config;
		this.contextPath = contextPath;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public ServletContext getContext(String uripath) {
		throw new UnsupportedOperationException();
	}

	public int getMajorVersion() {
		return 2;
	}

	public int getMinorVersion() {
		return 5;
	}

	public String getMimeType(String file) {
		return mimeTypesMap.getContentType(file);
	}

	public Set<String> getResourcePaths(String path) {
		throw new UnsupportedOperationException();
	}

	public URL getResource(String path) throws MalformedURLException {
		return null;
	}

	public InputStream getResourceAsStream(String path) {
		 return null;
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		throw new UnsupportedOperationException();
	}

	public RequestDispatcher getNamedDispatcher(String name) {
		throw new UnsupportedOperationException();
	}

	public Servlet getServlet(String name) throws ServletException {
		throw new UnsupportedOperationException();
	}

	public Enumeration<Servlet> getServlets() {
		throw new UnsupportedOperationException();
	}

	public Enumeration<String> getServletNames() {
		throw new UnsupportedOperationException();
	}

	public void log(String msg) {
		log.info(msg);
	}

	public void log(Exception exception, String msg) {
		log.info(msg, exception);

	}

	public void log(String message, Throwable throwable) {
		log.info(message, throwable);

	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	public String getServerInfo() {
		String osName    = System.getProperty("os.name");
	    String osVersion = System.getProperty("os.version");
	    String osArch    = System.getProperty("os.arch");
	    String os = osName + " " + osVersion + "/" + osArch;
	    return "Hypersocket Servlet Container " + os;
	}

	public String getInitParameter(String name) {
		return config.getInitParameter(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		return config.getInitParameterNames();
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		return new Vector<String>(attributes.keySet()).elements();
	}

	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public String getServletContextName() {
		return "Hypersocket Context";
	}

}
