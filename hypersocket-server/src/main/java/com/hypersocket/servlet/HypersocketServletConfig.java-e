/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketServletConfig implements ServletConfig {

	String servletName;
	Properties initParameters;
	ServletContext servletContext;
	
	static Logger log = LoggerFactory.getLogger(HypersocketServletConfig.class);
	
	public HypersocketServletConfig(String servletName, String contextPath) {
	    this.servletName = servletName;
		initParameters = new Properties();
	    servletContext = new HypersocketServletContext(this, contextPath);
    }

	public String getServletName() {
		return servletName;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getInitParameter(String name) {
		return initParameters.getProperty(name);
	}

	public Enumeration<Object> getInitParameterNames() {
		return initParameters.keys();
	}
}
