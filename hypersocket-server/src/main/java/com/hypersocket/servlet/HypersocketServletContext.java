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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

import com.hypersocket.server.HypersocketServer;

public class HypersocketServletContext implements ServletContext {

	Map<String,Object> attributes = new HashMap<String,Object>();
	static Logger log = LoggerFactory.getLogger(HypersocketServletContext.class);
	ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
	Properties initParameters;
	List<HypersocketServletConfig> configs = new ArrayList<>();
	HypersocketServer server;
	public HypersocketServletContext(HypersocketServer server) {
		initParameters = new Properties();
		this.server = server;
	}
	
	public List<HypersocketServletConfig> getServletConfigs() {
		return configs;
	}

	public void addServlet(HypersocketServletConfig hypersocketServletConfig) {
		configs.add(hypersocketServletConfig);
	}
	
	public String getContextPath() {
		return server.getBasePath();
	}

	public ServletContext getContext(String uripath) {
		throw new UnsupportedOperationException();
	}

	public int getMajorVersion() {
		return 3;
	}

	public int getMinorVersion() {
		return 0;
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
		return initParameters.getProperty(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<String> getInitParameterNames() {
		return (Enumeration<String>) initParameters.propertyNames();
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

	@Override
	public int getEffectiveMajorVersion() {
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		if(initParameters.containsKey(name)){
			return false;
		}
		initParameters.put(name, value);
		return true;
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void declareRoles(String... roleNames) {
		throw new UnsupportedOperationException();
	}

}
