/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

public class HypersocketSessionFactory {

	static HypersocketSessionFactory instance = new HypersocketSessionFactory();
	
	private Map<String,HypersocketSession> sessions = Collections.synchronizedMap(new HashMap<String,HypersocketSession>());
	
	public static HypersocketSessionFactory getInstance() {
		return instance;
	}
	
	public HypersocketSession getSession(String sessionid, ServletContext context) {
		return sessions.get(sessionid);
	}
	
	public HypersocketSession createSession(ServletContext context) {
		HypersocketSession session =  new HypersocketSession(context) {
			@Override
			public void invalidate() {
				super.invalidate();
				sessions.remove(getId());
			}
		};
		sessions.put(session.getId(), session);
		return session;
	}
}
