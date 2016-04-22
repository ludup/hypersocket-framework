/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

public class HypersocketSessionFactory {

	static HypersocketSessionFactory instance = new HypersocketSessionFactory();
	
	Map<String,HypersocketSession> sessions = new HashMap<String,HypersocketSession>();
	
	public static HypersocketSessionFactory getInstance() {
		return instance;
	}
	
	public HypersocketSession getSession(String sessionid, ServletContext context) {
		return sessions.get(sessionid);
	}
	
	public HypersocketSession createSession(ServletContext context) {
		HypersocketSession session =  new HypersocketSession(context);
		sessions.put(session.getId(), session);
		return session;
	}
}
