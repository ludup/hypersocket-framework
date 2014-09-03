/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.events.SessionClosedEvent;
import com.hypersocket.session.events.SessionOpenEvent;

@Service
public class SessionServiceImpl extends AuthenticatedServiceImpl implements
		SessionService {

	@Autowired
	SessionRepository repository;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	SchedulerService schedulerService;
	
	@Autowired
	EventService eventService;
	
	static final String SESSION_TIMEOUT = "session.timeout";

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	UserAgentStringParser parser;

	Map<Session,List<ResourceSession<?>>> resourceSessions = new HashMap<Session,List<ResourceSession<?>>>();
	Map<String,SessionResourceToken<?>> sessionTokens = new HashMap<String,SessionResourceToken<?>>();
	
	@PostConstruct
	private void registerConfiguration() throws AccessDeniedException {

		if(log.isInfoEnabled()) {
			log.info("Loading User Agent database");
		}
		parser = UADetectorServiceFactory.getCachingAndUpdatingParser();
		if(log.isInfoEnabled()) {
			log.info("Loaded User Agent database");
		}
		
		eventService.registerEvent(SessionOpenEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SessionClosedEvent.class, RESOURCE_BUNDLE);
		
		if(log.isInfoEnabled()) {
			log.info("Scheduling session reaper job");
		}
		
		try {
			schedulerService.scheduleIn(SessionReaperJob.class, null, 1, 60000);
		} catch (SchedulerException e) {
			log.error("Failed to schedule session reaper job", e);
		}
	}

	@Override
	public Session openSession(String remoteAddress, 
			Principal principal,
			AuthenticationScheme completedScheme, 
			String userAgent) {
		
		if(userAgent==null) {
			userAgent = "Unknown";
		}
		ReadableUserAgent ua = parser.parse(userAgent);
		
		Session session = repository.createSession(remoteAddress, principal,
				completedScheme, ua.getFamily().getName(), ua.getVersionNumber().toVersionString(), 
				ua.getOperatingSystem().getFamily().getName(),
				ua.getOperatingSystem().getVersionNumber().toVersionString(),
				configurationService.getIntValue(SESSION_TIMEOUT));
		
		eventService.publishEvent(new SessionOpenEvent(this, session));
		return session;
	}

	@Override
	public Session getSession(String id) {
		Session session = repository.getSessionById(id);

		// Check for a null last updated indicating transient info needs setting
		if (session!=null && !session.hasLastUpdated()) {
			session.setTimeout(configurationService
					.getIntValue(SESSION_TIMEOUT));

			// We don't set last updated, let the session service work out if
			// its timed out from last modified.
		}
		return session;
	}

	@Override
	public synchronized boolean isLoggedOn(Session session, boolean touch) {
		if (session == null)
			return false;

		if (session.getSignedOut() == null) {

			Calendar currentTime = Calendar.getInstance();
			Calendar c = Calendar.getInstance();
			c.setTime(session.getLastUpdated());
			c.add(Calendar.MINUTE, session.getTimeout());

			if (log.isDebugEnabled()) {
				log.debug("Checking session timeout currentTime="
						+ currentTime.getTime() + " lastUpdated="
						+ session.getLastUpdated() + " timeoutThreshold="
						+ c.getTime());
			}

			if (c.before(currentTime)) {
				if (log.isDebugEnabled()) {
					log.debug("Session has timed out");
				}
				closeSession(session);

				if (log.isDebugEnabled()) {
					log.debug("Session "
							+ session.getPrincipal().getPrincipalName() + "/"
							+ session.getId() + " is now closed");
				}

				return false;
			}

			if (touch) {
				session.touch();
				if(session.isReadyForUpdate()) {
					repository.updateSession(session);
					if(log.isDebugEnabled()) {
						log.debug("Session "
								+ session.getPrincipal().getPrincipalName() + "/"
								+ session.getId() + " state has been updated");
					}
				}
				
			}
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void closeSession(Session session) {

		if (session.getSignedOut() != null) {
			throw new IllegalArgumentException(
					"Attempting to close a session which is already closed!");
		}
		
		if(resourceSessions.containsKey(session)) {
			List<ResourceSession<?>> rs = resourceSessions.remove(session);
			for(ResourceSession<?> s : rs) {
				s.close();
			}
		}

		session.setSignedOut(new Date());
		repository.updateSession(session);
		eventService.publishEvent(new SessionClosedEvent(this, session));

	}

	@Override
	public void switchRealm(Session session, Realm realm)
			throws AccessDeniedException {

		assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);

		if(log.isInfoEnabled()) {
			log.info("Switching " + session.getPrincipal().getName() + " to " + realm.getName() + " realm");
		}
		
		session.setCurrentRealm(realm);
		repository.updateSession(session);
	}

	@Override
	public void registerResourceSession(Session session, ResourceSession<?> resourceSession) {
		
		if(!resourceSessions.containsKey(session)) {
			resourceSessions.put(session, new ArrayList<ResourceSession<?>>());
		}
		resourceSessions.get(session).add(resourceSession);
	}
	
	@Override
	public boolean hasResourceSession(Session session, Resource resource) {
		if(resourceSessions.containsKey(session)) {
			List<ResourceSession<?>> rs = resourceSessions.get(session);
			for(ResourceSession<?> s : rs) {
				if(s.getResource().equals(resource)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void unregisterResourceSession(Session session,
			ResourceSession<?> resourceSession) {
		
		if(resourceSessions.containsKey(session)) {
			resourceSessions.get(session).remove(resourceSession);
		}
	}

	@Override
	public List<Session> getActiveSessions() throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getActiveSessions();
	}
	
	@Override
	public <T> SessionResourceToken<T> createSessionToken(T resource) {
		
		SessionResourceToken<T> token = new SessionResourceToken<T>(getCurrentSession(), resource);
		sessionTokens.put(token.getShortCode(), token);
		return token;
	}
	
	@Override
	public <T> SessionResourceToken<T> getSessionToken(String shortCode, Class<T> resourceClz) {
		
		if(sessionTokens.containsKey(shortCode)) {
			
			@SuppressWarnings("unchecked")
			SessionResourceToken<T> token = (SessionResourceToken<T>) sessionTokens.get(shortCode);
			
			if(isLoggedOn(token.getSession(), true)) {
				return token;
			}
		}
		
		return null;
	}
	
	
}
