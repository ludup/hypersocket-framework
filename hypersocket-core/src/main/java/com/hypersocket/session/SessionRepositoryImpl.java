/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.RealmOrSystemRealmCriteria;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;
import com.hypersocket.utils.HypersocketUtils;

@Repository
public class SessionRepositoryImpl extends AbstractEntityRepositoryImpl<Session,String> implements SessionRepository {

	static Logger log = LoggerFactory.getLogger(SessionRepositoryImpl.class);
	
	public SessionRepositoryImpl() {
		super(true);
	}
	
	@Override
	@Transactional
	public Session createSession(String remoteAddress, 
			Principal principal,
			AuthenticationScheme scheme, 
			String userAgent, 
			String userAgentVersion,
			String os,
			String osVersion,
			Map<String, String> parameters,
			int timeout,
			Realm realm) {

		Session session = new Session();
		session.setPrincipal(principal);
		session.setRemoteAddress(remoteAddress);
		session.setUserAgent(HypersocketUtils.checkNull(userAgent, "unknown"));
		session.setUserAgentVersion(HypersocketUtils.checkNull(userAgentVersion, "unknown"));
		session.setOs(HypersocketUtils.checkNull(os, "unknown"));
		session.setOsVersion(HypersocketUtils.checkNull(osVersion, "unknown"));
		session.setAuthenticationScheme(scheme);
		session.setTimeout(timeout);
		session.setPrincipalRealm(realm);
		session.setSystem(false);
		
		if (parameters != null && !parameters.isEmpty()) {
			session.setStateParameters(parameters);
		}
		
		save(session);
		return session;
	}

	@Override
	@Transactional(readOnly=true)
	public Session getSessionById(String id) {
		return get("id", id, Session.class);
	}

	@Override
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
	public List<Session> getPrincipalActiveSessions(Principal principal) {
		return allEntities(Session.class, (c) -> {
			c.add(Restrictions.eq("principal", principal))
				.add(Restrictions.isNull("signedOut"))
				.add(Restrictions.eq("system", false));
		});
	}
	
	@Override
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
	public Long getActiveSessionCount(boolean distinctUsers, Realm realm) {
		Criteria criteria = createCriteria(Session.class);

		criteria.add(Restrictions.isNull("signedOut"));
		criteria.add(Restrictions.eq("system", false));
		criteria.add(Restrictions.eq("realm", realm));
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		
		if(distinctUsers) {
			criteria.setProjection(Projections.countDistinct("principal"));
		} else {
			criteria.setProjection(Projections.rowCount());
		}
		return (long) criteria.uniqueResult();
	}
	
	@Override
	@Transactional(readOnly=true)
	public Long getActiveSessionCount() {
		return getActiveSessionCount(false);
	}
	
	@Override
	@Transactional(readOnly=true)
	public Map<String,Long> getBrowserCount() {
		List<?> ret = getCounts(Session.class, "userAgent", true, 5, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("system", false));
				criteria.add(Restrictions.not(Restrictions.eq("userAgent", "unknown")));
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
	@Transactional(readOnly=true)
	public Map<String,Long> getBrowserCount(final Date startDate, final Date endDate) {

		List<?> ret = getCounts(Session.class, "userAgent", true, 5, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
				criteria.add(Restrictions.not(Restrictions.eq("userAgent", "unknown")));
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
	@Transactional(readOnly=true)
	public Map<String,Long> getBrowserCount(final Date startDate, final Date endDate, final Realm realm) {

		List<?> ret = getCounts(Session.class, "userAgent", true, 5, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.not(Restrictions.eq("userAgent", "unknown")));
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
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
	public Map<String,Long> getOSCount(final Date startDate, final Date endDate) {

		List<?> ret = getCounts(Session.class, "os", true, 5, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
				criteria.add(Restrictions.not(Restrictions.eq("os", "")));
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
	@Transactional(readOnly=true)
	public Map<String,Long> getOSCount(final Date startDate, final Date endDate, final Realm realm) {

		List<?> ret = getCounts(Session.class, "os", true, 5, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.ge("created", startDate));
				criteria.add(Restrictions.lt("created", endDate));
				criteria.add(Restrictions.eq("system", false));
				criteria.add(Restrictions.not(Restrictions.eq("os", "")));
				criteria.add(Restrictions.eq("realm", realm));
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
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
	public Long getSessionCount(final Date startDate, final Date endDate, final boolean distinctUsers, final Realm realm) {

		Criteria criteria = createCriteria(Session.class);
		
		criteria.add(Restrictions.or(
				Restrictions.and(Restrictions.ge("created", startDate), Restrictions.lt("created", endDate)),
				Restrictions.and(Restrictions.lt("created", startDate), Restrictions.or(
						Restrictions.ge("signedOut", startDate), Restrictions.isNull("signedOut")))));

		criteria.add(Restrictions.eq("system", false));
		
		criteria.add(Restrictions.eq("realm", realm));
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		
		if(distinctUsers) {
			criteria.setProjection(Projections.countDistinct("principal"));
		} else {
			criteria.setProjection(Projections.rowCount());
		}
		return (long) criteria.uniqueResult();
	}
	
	@Override
	@Transactional(readOnly=true)
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
	
	@Override
	@Transactional(readOnly=true)
	public List<Session> search(Realm realm, final String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return super.search(getEntityClass(), "uuid", "", start,
				length, sorting, ArrayUtils.addAll(configs,
						new RealmOrSystemRealmCriteria(realm),
						new CriteriaConfiguration() {

							@Override
							public void configure(Criteria criteria) {
																
								criteria.add(Restrictions.eq("system", false));
								criteria.add(Restrictions.isNull("signedOut"));
								
								if(StringUtils.isNotEmpty(searchPattern)) {
									criteria = criteria.createCriteria("principal");
									criteria.add(Restrictions.ilike("name", searchPattern));
								}
								
							}
				        }));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm, final String searchPattern,
			CriteriaConfiguration... configs) {
		return getCount(getEntityClass(), "uuid", "",
				ArrayUtils.addAll(configs, new RealmOrSystemRealmCriteria(
						realm),
						new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("system", false));
						criteria.add(Restrictions.isNull("signedOut"));
						
						if(StringUtils.isNotEmpty(searchPattern)) {
							criteria = criteria.createCriteria("principal");
							criteria.add(Restrictions.ilike("name", searchPattern));
						}
					}
		        }));
	}

	@Override
	@Transactional(readOnly=true)
	public Map<String,Long> getPrincipalUsage(final Realm realm, final int maximumUsers, final Date startDate, final Date endDate) {
		
		List<?> ret = sum(Session.class, "principal", Sort.DESC, new RealmOrSystemRealmCriteria(
				realm), new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						
						criteria.add(Restrictions.or(
								Restrictions.and(Restrictions.ge("created", startDate), Restrictions.lt("created", endDate)),
								Restrictions.and(Restrictions.lt("created", startDate), Restrictions.or(
										Restrictions.ge("signedOut", startDate), Restrictions.isNull("signedOut")))));

						
						criteria.add(Restrictions.eq("system", false));
						criteria.setMaxResults(maximumUsers);
					}
			
		});
		
		Map<String,Long> results = new HashMap<String,Long>();
		for(Object obj : ret) {
			Object[] tmp = (Object[])obj;
			results.put(((Principal) tmp[0]).getDescription(), ((Long)tmp[1] / 60));
		}
		
		return results;
	}

	

	@Override
	@Transactional
	public void signOutActive() {
		Query query = createQuery("update Session s set signedOut = :signedOut where signedOut is null", true);
		query.setTimestamp("signedOut", new Date());
		query.executeUpdate();
	}
	

	@Override
	@Transactional
	public void deleteRealm(final Realm realm) {
		
		Collection<Session> sessions = list(Session.class, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.createAlias("impersonatedPrincipal", "i");
				criteria.add(Restrictions.eq("i.realm", realm));
			}
		});
		
		for(Session session : sessions) {
			delete(session);
		}
		
		Query q = createQuery("delete from Session where realm = :r or currentRealm = :r", true);
		q.setParameter("r", realm);
		log.info(String.format("Deleted %d Session", q.executeUpdate()));
	}

	@Override
	public void cleanUp(Date maxDate) {
		
		Query query = createQuery("delete from Session where created < :maxDate", true);
		query.setTimestamp("maxDate", maxDate);
		
		query.executeUpdate();
		
	}

	@Override
	@Transactional
	public void saveSession(Session session) {
		saveEntity(session);
	}

	@Override
	public void updateRealmSessions(Realm realm) {
		/* Managed by hibernate */
	}

	@Override
	public void updatePrincipalSessions(Principal principal) {
		/* Managed by hibernate */
	}
}
