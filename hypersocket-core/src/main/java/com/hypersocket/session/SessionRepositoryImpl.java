/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
@Transactional
public class SessionRepositoryImpl extends AbstractEntityRepositoryImpl<Session,String> implements SessionRepository {

	static Logger log = LoggerFactory.getLogger(SessionRepositoryImpl.class);
	
	@Override
	public Session createSession(String remoteAddress, 
			Principal principal, 
			AuthenticationScheme scheme, 
			String userAgent, 
			String userAgentVersion,
			String os,
			String osVersion,
			int timeout) {

		Session session = new Session();
		session.setPrincipal(principal);
		session.setRemoteAddress(remoteAddress);
		session.setUserAgent(userAgent);
		session.setUserAgentVersion(userAgentVersion);
		session.setOs(os);
		session.setOsVersion(osVersion);
		session.setAuthenticationScheme(scheme);
		session.setTimeout(timeout);
		save(session);
		return session;
	}

	@Override
	public void updateSession(Session session) {
		
		if(log.isDebugEnabled()) {
			log.debug("Updating session " + session.getId() + " lastUpdated=" + session.getLastUpdated().getTime());
		}
		save(session);
	}

	@Override
	public Session getSessionById(String id) {
		return get("id", id, Session.class);
	}

	@Override
	public List<Session> getActiveSessions() {
		return allEntities(Session.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.isNull("signedOut"));
			}
		});
	}

	@Override
	protected Class<Session> getEntityClass() {
		return Session.class;
	}

}
