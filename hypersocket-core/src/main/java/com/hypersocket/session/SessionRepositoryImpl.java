/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
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
	
	public SessionRepositoryImpl() {
		super(true);
	}
	
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
		session.system = false;
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
				criteria.add(Restrictions.eq("system", false));
			}
		});
	}
	
	@Override
	public Long getActiveSessionCount(boolean distinctUsers) {
		Criteria criteria = createCriteria(Session.class);

		criteria.add(Restrictions.isNull("signedOut"));
		criteria.add(Restrictions.eq("system", false));
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		
		if(distinctUsers) {
			criteria.setProjection(Projections.countDistinct("principal"));
		} else {
			criteria.setProjection(Projections.rowCount());
		}
		return (long) criteria.uniqueResult();
	}
	
	@Override
	public Long getActiveSessionCount() {
		return getActiveSessionCount(false);
	}
	
	@Override
	public Map<String,Long> getBrowserCount() {
		List<?> ret = getCounts(Session.class, "userAgent", new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("system", false));
			}
		});
		
		Map<String,Long> results = new HashMap<String,Long>();
		for(Object obj : ret) {
			Object[] tmp = (Object[])obj;
			results.put((String) tmp[0], (Long)tmp[1]);
		}
		
		return results;
	}
	
	@Override
	public Map<String,Long> getBrowserCount(final Date startDate, final Date endDate) {

		List<?> ret = getCounts(Session.class, "userAgent", new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
			}
		});
		
		Map<String,Long> results = new HashMap<String,Long>();
		for(Object obj : ret) {
			Object[] tmp = (Object[])obj;
			results.put((String) tmp[0], (Long)tmp[1]);
		}
		
		return results;
	}
	
	@Override
	public Map<String,Long> getIPCount(final Date startDate, final Date endDate) {

		List<?> ret = getCounts(Session.class, "remoteAddress", new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
			}
		});
		
		Map<String,Long> results = new HashMap<String,Long>();
		for(Object obj : ret) {
			Object[] tmp = (Object[])obj;
			results.put((String) tmp[0], (Long)tmp[1]);
		}
		
		return results;
	}
	
	@Override
	public Map<String,Long> getOSCount(final Date startDate, final Date endDate) {

		List<?> ret = getCounts(Session.class, "os", new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
			}
		});
		
		Map<String,Long> results = new HashMap<String,Long>();
		for(Object obj : ret) {
			Object[] tmp = (Object[])obj;
			results.put((String) tmp[0], (Long)tmp[1]);
		}
		
		return results;
	}
	
	@Override
	public Long getSessionCount(final Date startDate, final Date endDate, final boolean distinctUsers) {

		Criteria criteria = createCriteria(Session.class);
		
		criteria.add(Restrictions.or(
				Restrictions.and(Restrictions.ge("created", startDate), Restrictions.lt("created", endDate)),
				Restrictions.and(Restrictions.lt("created", startDate), Restrictions.or(
						Restrictions.ge("signedOut", startDate), Restrictions.isNull("signedOut")))));

		criteria.add(Restrictions.eq("system", false));
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		
		if(distinctUsers) {
			criteria.setProjection(Projections.countDistinct("principal"));
		} else {
			criteria.setProjection(Projections.rowCount());
		}
		return (long) criteria.uniqueResult();
	}
	
	@Override
	public List<Session> getSystemSessions() {
		return allEntities(Session.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.isNull("signedOut"));
				criteria.add(Restrictions.eq("system", true));
			}
		});
	}

	@Override
	protected Class<Session> getEntityClass() {
		return Session.class;
	}

}
