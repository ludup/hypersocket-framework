/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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

	private String servletName;
	private Properties initParameters;
	private ServletContext servletContext;
	
	static Logger log = LoggerFactory.getLogger(HypersocketServletConfig.class);
	
	public HypersocketServletConfig(String servletName, ServletContext context) {
	    this.servletName = servletName;
	    this.servletContext = context;
		initParameters = new Properties();
		((HypersocketServletContext)context).addServlet(this);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration<String> getInitParameterNames() {
		Enumeration params = initParameters.keys();
		return params;
	}
	
	public void setInitParameter(String key, String value) {
		initParameters.setProperty(key, value);
	}
}
