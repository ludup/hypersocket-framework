/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.decibel.uasparser.OnlineUpdater;
import com.decibel.uasparser.UASparser;
import com.decibel.uasparser.UserAgentInfo;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.events.SessionClosedEvent;
import com.hypersocket.session.events.SessionEvent;
import com.hypersocket.session.events.SessionOpenEvent;
import com.hypersocket.tables.ColumnSort;

@Service
public class SessionServiceImpl extends PasswordEnabledAuthenticatedServiceImpl
		implements SessionService, ApplicationListener<ContextStartedEvent> {

	@Autowired
	SessionRepository repository;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	RealmService realmService;

	@Autowired
	EventService eventService;

	static final String SESSION_TIMEOUT = "session.timeout";

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	public static String TOKEN_PREFIX = "_TOK";

	UASparser parser;
	OnlineUpdater updater;
	
	Map<Session, List<ResourceSession<?>>> resourceSessions = new HashMap<Session, List<ResourceSession<?>>>();
	Map<String, SessionResourceToken<?>> sessionTokens = new HashMap<String, SessionResourceToken<?>>();

	Map<String, Session> nonCookieSessions = new HashMap<String, Session>();

	Session systemSession;

	@PostConstruct
	private void postConstruct() throws AccessDeniedException {

		if (log.isInfoEnabled()) {
			log.info("Loading User Agent database");
		}
		
		parser = new UASparser();
		updater = new OnlineUpdater(parser, "");
		
		if (log.isInfoEnabled()) {
			log.info("Loaded User Agent database");
		}

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "session.category");
		
		for(SessionPermission perm : SessionPermission.values()) {
			permissionService.registerPermission(perm, cat);
		}
		
		eventService.registerEvent(SessionEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SessionOpenEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SessionClosedEvent.class, RESOURCE_BUNDLE);
		
	
	}

	private Session createSystemSession() {
		Session session = new Session();
		session.setId(UUID.randomUUID().toString());
		session.setCurrentRealm(realmService.getSystemRealm());
		session.setOs(System.getProperty("os.name"));
		session.setOsVersion(System.getProperty("os.version"));
		session.setPrincipal(realmService.getSystemPrincipal());
		session.setUserAgent("N/A");
		session.setUserAgentVersion("N/A");
		session.setRemoteAddress("N/A");
		session.system = true;

		repository.saveEntity(session);
		return session;
	}

	@Override
	public Session getSystemSession() {
		if (systemSession == null) {
			systemSession = createSystemSession();
		}
		return systemSession;
	}

	@Override
	public Session openSession(String remoteAddress, Principal principal,
			AuthenticationScheme completedScheme, String userAgent,
			Map<String, String> parameters) {
		return openSession(remoteAddress, principal, completedScheme, userAgent, parameters, principal.getRealm());
	}
	
	@Override
	public Session openSession(String remoteAddress, Principal principal,
			AuthenticationScheme completedScheme, String userAgent,
			Map<String, String> parameters, Realm realm) {

		Session session = null;
		
		if (userAgent == null) {
			userAgent = "Unknown";
		} else if(userAgent.startsWith("Hypersocket-Client")) {
			
			String[] values = userAgent.split(";");
			
			String agent = "Hypersocket Client";
			String agentVersion = "1.0"; 
			if(values.length > 1) {
				agentVersion = values[1];
			}
			String os = "Unknown";
			if(values.length > 2) {
				if(values[2].toLowerCase().startsWith("windows")) {
					os = "Windows";
				} else if(values[2].toLowerCase().startsWith("mac os x")) {
					os = "OS X";
				} else if(values[2].toLowerCase().startsWith("linux")) {
					os = "Linux";
				} else {
					os = values[2];
				}
			}
			String osVersion = "Unknown";
			if(values.length > 3) {
				osVersion = values[3];
			}
			
			session = repository.createSession(remoteAddress, principal,
					completedScheme, agent, agentVersion, os, osVersion, 
					configurationService.getIntValue(
					realm, SESSION_TIMEOUT), realm);
		} else {
			UserAgentInfo info;
			try {
				info = parser.parse(userAgent);
				session = repository.createSession(remoteAddress, principal,
						completedScheme, 
						info.getUaFamily(), 
						info.getBrowserVersionInfo(), 
						info.getOsFamily(),
						info.getOsName(), configurationService.getIntValue(
								realm, SESSION_TIMEOUT), realm);
				
			} catch (IOException e) {
				session = repository.createSession(remoteAddress, principal,
						completedScheme, "Unknown", "Unknown", "Unknown", "Unknown", 
						configurationService.getIntValue(
						realm, SESSION_TIMEOUT), realm);
			}

			
		}
		
		eventService.publishEvent(new SessionOpenEvent(this, session));
		return session;
	}

	@Override
	public Session getSession(String id) {
		return repository.getSessionById(id);
	}

	@Override
	public synchronized boolean isLoggedOn(Session session, boolean touch) {
		if (session == null)
			return false;

		repository.refresh(session);
		
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
				if (session.isReadyForUpdate()) {
					repository.updateSession(session);
					if (log.isDebugEnabled()) {
						log.debug("Session "
								+ session.getPrincipal().getPrincipalName()
								+ "/" + session.getId()
								+ " state has been updated");
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
			log.error("Attempting to close a session which is already closed!");
			return;
		}

		if (nonCookieSessions.containsKey(session.getNonCookieKey())) {
			nonCookieSessions.remove(session.getNonCookieKey());
		}

		if (resourceSessions.containsKey(session)) {
			List<ResourceSession<?>> rs = resourceSessions.remove(session);
			for (ResourceSession<?> s : rs) {
				s.close();
			}
		}

		synchronized (sessionTokens) {
			Set<String> tokens = new HashSet<String>();
			for (SessionResourceToken<?> token : sessionTokens.values()) {
				if (token.getSession().equals(session)) {
					tokens.add(token.getShortCode());
				}
			}

			for (String t : tokens) {
				sessionTokens.remove(t);
			}
		}

		session.setSignedOut(new Date());
		session.setNonCookieKey(null);
		repository.updateSession(session);

		if (!session.isSystem()) {
			eventService.publishEvent(new SessionClosedEvent(this, session));
		}
	}

	@Override
	public void switchRealm(Session session, Realm realm)
			throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION,
				SystemPermission.SYSTEM, SystemPermission.SWITCH_REALM);

		if (log.isInfoEnabled()) {
			log.info("Switching " + session.getPrincipal().getName() + " to "
					+ realm.getName() + " realm");
		}

		session.setCurrentRealm(realm);
		repository.updateSession(session);
	}

	protected void assertImpersonationPermission() throws AccessDeniedException {
		
		elevatePermissions(RolePermission.READ);
		
		try {
		if (hasSessionContext()) {
			if (getCurrentSession().isImpersonating()) {
				
				try {
					if(permissionService.hasRole(getCurrentSession().getPrincipal(), 
							permissionService.getRole(PermissionService.ROLE_ADMINISTRATOR, 
									getCurrentSession().getPrincipal().getRealm()))) {
						return;
					}
				} catch (ResourceNotFoundException e) {
				}
				
				verifyPermission(getCurrentSession().getPrincipal(),
						PermissionStrategy.EXCLUDE_IMPLIED,
						SystemPermission.SYSTEM_ADMINISTRATION,
						SystemPermission.SYSTEM);
				
				return;
			}
		}

		try {
			if(permissionService.hasRole(getCurrentPrincipal(), 
					permissionService.getRole(PermissionService.ROLE_ADMINISTRATOR, 
							getCurrentPrincipal().getRealm()))) {
				return;
			}
		} catch (ResourceNotFoundException e) {
		}
		
		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION,
				SystemPermission.SYSTEM);
		
		} finally {
			clearElevatedPermissions();
		}

	}

	@Override
	public void switchPrincipal(Session session, Principal principal)
			throws AccessDeniedException {
		switchPrincipal(session, principal, false);
	}

	@Override
	public void switchPrincipal(Session session, Principal principal,
			boolean inheritPermissions) throws AccessDeniedException {

		assertImpersonationPermission();

		if (log.isInfoEnabled()) {
			log.info("Switching " + session.getPrincipal().getName() + " to "
					+ principal.getName());
		}

		session.setImpersonatedPrincipal(principal);
		session.setInheritPermissions(inheritPermissions);

		setCurrentSession(session, getCurrentLocale());
		repository.updateSession(session);
	}

	@Override
	public void registerResourceSession(Session session,
			ResourceSession<?> resourceSession) {

		if (!resourceSessions.containsKey(session)) {
			resourceSessions.put(session, new ArrayList<ResourceSession<?>>());
		}
		resourceSessions.get(session).add(resourceSession);
	}

	@Override
	public boolean hasResourceSession(Session session, Resource resource) {
		if (resourceSessions.containsKey(session)) {
			List<ResourceSession<?>> rs = resourceSessions.get(session);
			for (ResourceSession<?> s : rs) {
				if (s.getResource().equals(resource)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void unregisterResourceSession(Session session,
			ResourceSession<?> resourceSession) {

		if (resourceSessions.containsKey(session)) {
			resourceSessions.get(session).remove(resourceSession);
		}
	}

	@Override
	public List<Session> getActiveSessions() throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM,
				SystemPermission.SYSTEM_ADMINISTRATION);

		return repository.getActiveSessions();
	}

	@Override
	public Long getActiveSessionCount(boolean distinctUsers) throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getActiveSessionCount(distinctUsers);
	}
	
	@Override
	public Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getSessionCount(startDate, endDate, distinctUsers);
		
	}

	@Override
	public Map<String,Long> getBrowserCount(Date startDate, Date endDate) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getBrowserCount(startDate, endDate);
		
	}

	@Override
	public Map<String,Long> getIPCount(Date startDate, Date endDate) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getIPCount(startDate, endDate);
		
	}
	
	@Override
	public Map<String,Long> getOSCount(Date startDate, Date endDate) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getOSCount(startDate, endDate);
		
	}
	
	@Override
	public Map<String,Long> getPrincipalUsage(Date startDate, Date endDate) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM, SystemPermission.SYSTEM_ADMINISTRATION);
		
		return repository.getPrincipalUsage(getCurrentRealm(), 5, startDate, endDate);
		
	}
	
	@Override
	public <T> SessionResourceToken<T> createSessionToken(T resource) {

		SessionResourceToken<T> token = new SessionResourceToken<T>(
				getCurrentSession(), resource);
		sessionTokens.put(token.getShortCode(), token);
		return token;
	}

	@Override
	public <T> SessionResourceToken<T> getSessionToken(String shortCode,
			Class<T> resourceClz) {

		if (sessionTokens.containsKey(shortCode)) {

			@SuppressWarnings("unchecked")
			SessionResourceToken<T> token = (SessionResourceToken<T>) sessionTokens
					.get(shortCode);

			if (isLoggedOn(token.getSession(), true)) {
				return token;
			}
		}

		return null;
	}

	@Override
	public <T> T getSessionTokenResource(String shortCode, Class<T> resourceClz) {

		if (sessionTokens.containsKey(shortCode)) {

			@SuppressWarnings("unchecked")
			SessionResourceToken<T> token = (SessionResourceToken<T>) sessionTokens
					.get(shortCode);

			if (isLoggedOn(token.getSession(), true)) {
				return token.getResource();
			}
		}

		return null;
	}

	@Override
	public Session getNonCookieSession(String remoteAddr, String requestHeader,
			String authenticationSchemeResourceKey)
			throws AccessDeniedException {

		String key = createNonCookieSessionKey(remoteAddr, requestHeader,
				authenticationSchemeResourceKey);

		Session session = nonCookieSessions.get(key);

		if (session != null) {
			if (!isLoggedOn(session, true)) {
				throw new AccessDeniedException();
			}
			return session;
		}
		throw new AccessDeniedException();
	}

	@Override
	public void registerNonCookieSession(String remoteAddr,
			String requestHeader, String authenticationSchemeResourceKey,
			Session session) {

		String key = createNonCookieSessionKey(remoteAddr, requestHeader,
				authenticationSchemeResourceKey);

		session.setNonCookieKey(key);
		nonCookieSessions.put(key, session);
	}

	private String createNonCookieSessionKey(String remoteAddr,
			String requestHeader, String authenticationSchemeResourceKey) {
		StringBuffer buf = new StringBuffer();
		buf.append(remoteAddr);
		buf.append("|");
		buf.append(requestHeader);
		buf.append("|");
		buf.append(authenticationSchemeResourceKey);
		return buf.toString();
	}

	@Override
	public void revertPrincipal(Session session) throws AccessDeniedException {

		assertImpersonationPermission();

		if (log.isInfoEnabled()) {
			log.info("Switching " + session.getCurrentPrincipal().getName()
					+ " to " + session.getPrincipal().getName());
		}

		session.setImpersonatedPrincipal(null);
		session.setInheritPermissions(false);

		setCurrentSession(session, getCurrentLocale());
		repository.updateSession(session);
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {

		executeInSystemContext(new Runnable() {

			@Override
			public void run() {
				if (log.isInfoEnabled()) {
					log.info("Scheduling session reaper job");
				}

				for (Session session : repository.getSystemSessions()) {
					if (systemSession != null && systemSession.equals(session)) {
						continue;
					}
					closeSession(session);
				}

				try {
					JobDataMap data = new JobDataMap();
					data.put("jobName", "firstRunSessionReaperJob");
					data.put("firstRun", true);
					
					schedulerService.scheduleNow(SessionReaperJob.class, data);
					
					data = new JobDataMap();
					data.put("jobName", "sessionReaperJob");
					
					schedulerService.scheduleIn(SessionReaperJob.class, data, 60000,
							60000);
				} catch (SchedulerException e) {
					log.error("Failed to schedule session reaper job", e);
				} 
			}
			
		});

	}

	@Override
	public List<?> searchResources(Realm realm, String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SessionPermission.READ);
		
		return repository.search(realm, searchPattern, start, length, sorting);
	}

	@Override
	public Long getResourceCount(Realm realm, String searchPattern) throws AccessDeniedException {
		
		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SessionPermission.READ);
		
		return repository.getResourceCount(realm, searchPattern);
	}

	@Override
	public void executeInSystemContext(Runnable r) {
		
		setCurrentSession(getSystemSession(), Locale.getDefault());
		try {
			r.run();
		} finally {
			clearPrincipalContext();
		}
	
	}
}
