/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import org.springframework.stereotype.Repository;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepositoryImpl;

@Repository
public class SessionRepositoryImpl extends AbstractRepositoryImpl<String> implements SessionRepository {

	@Override
	public Session createSession(String remoteAddress, 
			Principal principal, 
			AuthenticationScheme scheme, 
			String userAgent, 
			String userAgentVersion,
			String os,
			String osVersion) {

		Session session = new Session();
		session.setPrincipal(principal);
		session.setRemoteAddress(remoteAddress);
		session.setUserAgent(userAgent);
		session.setUserAgentVersion(userAgentVersion);
		session.setOs(os);
		session.setOsVersion(osVersion);
		session.setAuthenticationScheme(scheme);
		save(session);
		return session;
	}

	@Override
	public void updateSession(Session session) {
		save(session);
	}

	@Override
	public Session getSessionById(String id) {
		return get("id", id, Session.class);
	}

}
