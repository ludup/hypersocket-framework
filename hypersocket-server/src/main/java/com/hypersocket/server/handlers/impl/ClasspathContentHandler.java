/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;

import com.hypersocket.utils.HypersocketUtils;


public class ClasspathContentHandler extends ContentHandlerImpl {

	static Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathContentHandler.class);
	
	private String classpathPrefix;
	private String basePath;
	private List<ClassLoader> loaders;

	public ClasspathContentHandler(String classpathPrefix, int priority) {
		this(classpathPrefix, priority, new ArrayList<>());
	}
	
	public ClasspathContentHandler(String classpathPrefix, int priority, List<ClassLoader> loaders) {
		super("classpath:" + classpathPrefix, priority);
		this.classpathPrefix = classpathPrefix;
		this.loaders = loaders;
	}
	
	public void addClassLoader(ClassLoader classLoader) {
		loaders.add(classLoader);
	}
	
	public void removeClassLoader(ClassLoader classLoader) {
		loaders.remove(classLoader);
	}
	
	public String getResourceName() {
		return "classpath";
	}
	
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	public String getBasePath() {
		return basePath;
	}
	
	@Override
	public InputStream getResourceStream(String path)
			throws FileNotFoundException {
		for(ClassLoader l : loaders) {
			InputStream res = l.getResourceAsStream(stripLeadingSlash(classpathPrefix + "/" + path));
			if(res != null)
				return res;
		}
		return getClass().getResourceAsStream(classpathPrefix + "/" + path);
	}

	@Override
	public long getResourceLength(String path) {
		return -1;
	}

	@Override
	public long getLastModified(String path) {
		try {
			
			URL url = null;
			for(ClassLoader l : loaders) {
				url = l.getResource(stripLeadingSlash(classpathPrefix + "/" + path));
				if(url != null)
					break;
			}
			if(url == null) {
				url = getClass().getResource(classpathPrefix + "/" + path);
			}
			if(url == null) {
				throw new IllegalStateException("No resource " + path);
			}
			if(log.isDebugEnabled()){
				log.debug("Processing resource URL " + url.toURI().toString());
			}
			int idx;
			File jarFile;
			if((idx = url.toURI().toString().indexOf("!")) > -1) {
				jarFile = new File(url.toURI().toString().substring(9, idx));
			} else {
				jarFile = new File(url.toURI());
			}
			
			if(log.isInfoEnabled()) {
				Date modified = new Date(jarFile.lastModified());
				if(log.isDebugEnabled()){
					log.debug("Jar file " + jarFile.getAbsolutePath() + " last modified " + HypersocketUtils.formatDateTime(modified));
				}
			}
			return jarFile.lastModified();
		} catch (URISyntaxException e) {
		}
		
		return System.currentTimeMillis();
	}

	@Override
	public int getResourceStatus(String path) throws RedirectException {

		for(ClassLoader l : loaders) {
			if(l.getResource(stripLeadingSlash(classpathPrefix + "/" + path)) != null) {
				return HttpStatus.SC_OK;
			}
		}
		URL url = getClass().getResource(classpathPrefix + "/" + path);
		if(url!=null) {
			return HttpStatus.SC_OK;
		} else {
			return HttpStatus.SC_NOT_FOUND;
		}
	}

	@Override
	public boolean getDisableCache() {
		return false;
	}

	String stripLeadingSlash(String input) {
		if(input.startsWith("/")) {
			return input.substring(1);
		}
		return input;
	}
}
