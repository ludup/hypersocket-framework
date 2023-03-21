/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketSessionFactory {
	
	static Logger log = LoggerFactory.getLogger(HypersocketServletContext.class);

	static HypersocketSessionFactory instance = new HypersocketSessionFactory();
	
	private Map<String,HypersocketSession> sessions = Collections.synchronizedMap(new HashMap<String,HypersocketSession>());

	{
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			var copy = new ArrayList<>(sessions.values());
			var it = copy.iterator();
			while(it.hasNext()) {
				var session = it.next();
				if(session.expired()) {
					session.invalidate();
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
	}
	
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
				synchronized(sessions) {
					log.info("Invalidating session {} of {}", getId(), sessions.size());
					sessions.remove(getId());
				}
			}
		};
		sessions.put(session.getId(), session);
		return session;
	}
}
