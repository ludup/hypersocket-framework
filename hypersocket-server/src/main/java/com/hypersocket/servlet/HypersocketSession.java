/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HypersocketSession implements HttpSession {

	private long creationTime = System.currentTimeMillis();
	private UUID uuid = UUID.randomUUID();
	private long lastAccessedTime = creationTime;
	private Map<String,Object> attributes = new HashMap<>();
	private boolean isNew = true;
	private int maxInterval = 300;
	private ServletContext context;
	
	HypersocketSession( ServletContext context) {
		this.context = context;
	}

	public void access() {
		lastAccessedTime = System.currentTimeMillis();
		isNew = false;
	}
	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return uuid.toString();
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInterval = interval;

	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInterval;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Vector<>(attributes.keySet()).elements();
	}

	@Override
	public String[] getValueNames() {
		return attributes.keySet().toArray(new String[0]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		if(value == null) {
			removeValue(name);
		}
		else {
			var original = attributes.put(name, value);
			if(original instanceof HttpSessionBindingListener) {
				((HttpSessionBindingListener)original).valueUnbound(new HttpSessionBindingEvent(this, name));
			}
			if(value instanceof HttpSessionBindingListener) {
				((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this, name));
			}
		}

	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		var original = attributes.remove(name);
		if(original instanceof HttpSessionBindingListener) {
			((HttpSessionBindingListener)original).valueUnbound(new HttpSessionBindingEvent(this, name));
		}
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
		while(!attributes.isEmpty()) {
			removeAttribute(attributes.keySet().iterator().next());
		}
	}

	public boolean isNew() {
		return isNew;
	}

	public boolean expired() {
		return System.currentTimeMillis() > lastAccessedTime + (getMaxInactiveInterval() * 1000);
	}

}
