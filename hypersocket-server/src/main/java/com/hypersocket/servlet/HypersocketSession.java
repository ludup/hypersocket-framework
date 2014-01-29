/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HypersocketSession implements HttpSession {

	long creationTime = System.currentTimeMillis();
	UUID uuid = UUID.randomUUID();
	long lastAccessedTime = creationTime;
	Map<String,Object> attributes = new HashMap<String,Object>();
	boolean invalidated = false;
	boolean isNew = true;
	int maxInterval = 300;
	ServletContext context;
	
	HypersocketSession( ServletContext context) {
		this.context = context;
	}
	
	void access() {
		lastAccessedTime = System.currentTimeMillis();
		isNew = false;
	}
	public long getCreationTime() {
		return creationTime;
	}

	public String getId() {
		return uuid.toString();
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public ServletContext getServletContext() {
		return context;
	}

	public void setMaxInactiveInterval(int interval) {
		this.maxInterval = interval;

	}

	public int getMaxInactiveInterval() {
		return maxInterval;
	}

	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException();
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Object getValue(String name) {
		return getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return new Vector<String>(attributes.keySet()).elements();
	}

	public String[] getValueNames() {
		return attributes.keySet().toArray(new String[0]);
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);

	}

	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public void removeValue(String name) {
		removeAttribute(name);
	}

	public void invalidate() {
		invalidated = true;

	}

	public boolean isNew() {
		return isNew;
	}

}
