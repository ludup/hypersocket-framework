/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.server.handlers.HttpRequestHandler;

public class FilterRequestHandler extends HttpRequestHandler {
	
	public interface FilterFactoryConfig extends FilterConfig {
		void setInitParameter(String name, String value);
	}
	
	public interface FilterFactory {

		Filter create(FilterFactoryConfig config);
		
	}

	static Logger log = LoggerFactory.getLogger(FilterRequestHandler.class);
	
	private List<Filter> filters = new ArrayList<>();
	
    public FilterRequestHandler() {
    	super("filters", Integer.MIN_VALUE);
    	for(var f : ServiceLoader.load(FilterFactory.class)) {
    		var parms = new HashMap<String, String>();
    		var fc = new FilterFactoryConfig() {
				
				@Override
				public ServletContext getServletContext() {
					return server.getServletContext();
				}
				
				@Override
				public Enumeration<String> getInitParameterNames() {
					return new Vector<>(parms.keySet()).elements();
				}
				
				@Override
				public String getInitParameter(String name) {
					return parms.get(name);
				}
				
				@Override
				public String getFilterName() {
					return f.getClass().getName();
				}

				@Override
				public void setInitParameter(String name, String value) {
					parms.put(name, value);
				}
			};
			var filter = f.create(fc);
			try {
				filter.init(fc);
				filters.add(filter);
			} catch (ServletException e) {
				throw new IllegalStateException("Failed to load filter.", e);
			}
    	}
    }
    
	@Override
	public void handleHttpRequest(HttpServletRequest request,
			HttpServletResponse response) {
		if(filters.isEmpty())
			return;
		try {
			doFilter(request, response, filters.iterator());
		}
		catch (Throwable e) {
			log.error("Servlet error", e);
			try {
				response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException ex) {
				log.error("IO error", ex);
			}
		} 
	}
	
	void doFilter(ServletRequest req, ServletResponse res, Iterator<Filter> it) throws IOException, ServletException {
		it.next().doFilter(req, res, (ireq, ires) -> {
			if(it.hasNext()) {
				doFilter(ireq, ires, it);
			}
		});
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return false;
	}

	@Override
	public boolean getDisableCache() {
		// TODO Auto-generated method stub
		return false;
	}
}
