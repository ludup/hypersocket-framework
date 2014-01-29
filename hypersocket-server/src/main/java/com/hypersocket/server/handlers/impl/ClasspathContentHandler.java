/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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

import org.apache.http.HttpStatus;


public class ClasspathContentHandler extends ContentHandlerImpl {

	String classpathPrefix;
	
	public ClasspathContentHandler(String classpathPrefix, int priority) {
		super("classpath:" + classpathPrefix, priority);
		this.classpathPrefix = classpathPrefix;
	}
	
	public String getResourceName() {
		return "classpath";
	}
	
	@Override
	public InputStream getResourceStream(String path)
			throws FileNotFoundException {
		return getClass().getResourceAsStream(classpathPrefix + "/" + path);
	}

	@Override
	public long getResourceLength(String path) {
		return -1;
	}

	@Override
	public long getLastModified(String path) {
		try {
			URL url = getClass().getResource(classpathPrefix + "/" + path);
			int idx;
			if((idx = url.toURI().toString().indexOf("!")) > -1) {
				return new File(url.toURI().toString().substring(0, idx)).lastModified();
			} else {
				return new File(url.toURI()).lastModified();
			}
		} catch (URISyntaxException e) {
		}
		
		return System.currentTimeMillis();
	}

	@Override
	public int getResourceStatus(String path) {
		
		URL url = getClass().getResource(classpathPrefix + "/" + path);
		
		if(url!=null) {
			return HttpStatus.SC_OK;
		} else {
			return HttpStatus.SC_NOT_FOUND;
		}
	}

}
