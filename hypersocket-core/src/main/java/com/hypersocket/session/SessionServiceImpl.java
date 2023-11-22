/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.cache.CacheService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.geo.GeoIPLocation;
import com.hypersocket.geo.GeoIPService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.realm.UserPermission;
import com.hypersocket.realm.events.UserImpersonatedEvent;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.JobData;
import com.hypersocket.session.events.ConcurrentSessionEvent;
import com.hypersocket.session.events.SessionClosedEvent;
import com.hypersocket.session.events.SessionEvent;
import com.hypersocket.session.events.SessionOpenEvent;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.utils.HttpUtils;

@Service
public class SessionServiceImpl extends PasswordEnabledAuthenticatedServiceImpl
		implements SessionService, ApplicationListener<ContextStartedEvent> {

	public static final String LOCATION_REGION_CODE = "location_region_code";

	public static final String LOCATION_COUNTRY_CODE = "location_country_code";

	public static final String LOCATION_LON = "location_lon";

	public static final String LOCATION_LAT = "location_lat";

	static final String SESSION_REAPER_JOB = "sessionReaperJob";

	static final String SESSION_CLEAN_UP_JOB = "sessionCleanUpJob";

	@Autowired
	private SessionRepository repository;
	
	private SessionStore store;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ClusteredSchedulerService schedulerService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private EventService eventService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired(required = false)
	private GeoIPService geoIpService;

	static final String SESSION_TIMEOUT = "session.timeout";

	private static final String SESSION_MAX_AGE = "security.sessionMaxAge";

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

	public static String TOKEN_PREFIX = "_TOK";

	private Map<Session, List<ResourceSession<?>>> resourceSessions = new HashMap<Session, List<ResourceSession<?>>>();
	private Map<String, String> nonCookieSessions = new HashMap<String, String>();
	private Session systemSession;
	private List<SessionReaperListener> listeners = new ArrayList<SessionReaperListener>();
	private List<CookieDecorator> cookieDecorators = Collections
			.synchronizedList(new ArrayList<>());
	
	@Autowired
	private HttpUtils httpUtils;
	
	@Autowired
	private CacheService cacheService; 
	
	ObjectMapper o = new ObjectMapper();

	@PostConstruct
	private void postConstruct() throws AccessDeniedException {

		if (log.isInfoEnabled()) {
			log.info("Loading User Agent database");
		}

		if (log.isInfoEnabled()) {
			log.info("Loaded User Agent database");
		}

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "session.category");

		for (SessionPermission perm : SessionPermission.values()) {
			permissionService.registerPermission(perm, cat);
		}

		eventService.registerEvent(SessionEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SessionOpenEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SessionClosedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserImpersonatedEvent.class, RESOURCE_BUNDLE);
		
		
		if(Boolean.getBoolean("logonbox.persistentSessions")) {
			store = repository;	
		}
		else {
			store = new InMemorySessionStore(repository);
			repository.signOutActive();
		}

	}

	@Override
	public void registerReaperListener(SessionReaperListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
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
		session.setRemoteAddress("");
		session.setSystem(true);

		store.saveSession(session);
		return session;
	}

	@Override
	public void updateSession(Session session) {
		if(log.isDebugEnabled()) {
			log.debug("Updating session " + session.getId() + " lastUpdated=" + session.getLastUpdated().getTime());
		}
		store.saveSession(session);
	}

	@Override
	public Session getSystemSession() {
		if (systemSession == null) {
			systemSession = createSystemSession();
		}
		return systemSession;
	}

	@Override
	public Session openSession(String remoteAddress, Principal principal, AuthenticationScheme completedScheme,
			String userAgent, Map<String, String> parameters) {
		return openSession(remoteAddress, principal, completedScheme, userAgent, parameters, principal.getRealm());
	}

	@Override
	public Session openSession(String remoteAddress, Principal principal, AuthenticationScheme completedScheme,
			String userAgent, Map<String, String> parameters, Realm realm) {

		Session session = null;
		
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}
		
		populateGeoInfoIfEnabled(remoteAddress, parameters, realm);

		if (userAgent == null) {
			userAgent = "Unknown";
		} else if (userAgent.startsWith("Hypersocket-Client")) {

			String[] values = userAgent.split(";");

			String agent = "Hypersocket Client";
			String agentVersion = "1.0";
			if (values.length > 1) {
				agentVersion = values[1];
			}
			String os = "Unknown";
			if (values.length > 2) {
				if (values[2].toLowerCase().startsWith("windows")) {
					os = "Windows";
				} else if (values[2].toLowerCase().startsWith("mac os x")) {
					os = "OS X";
				} else if (values[2].toLowerCase().startsWith("linux")) {
					os = "Linux";
				} else {
					os = values[2];
				}
			}
			String osVersion = "Unknown";
			if (values.length > 3) {
				osVersion = values[3];
			}

			session = store.createSession(remoteAddress, principal, completedScheme, agent, agentVersion, os,
					osVersion, parameters, configurationService.getIntValue(realm, SESSION_TIMEOUT), realm);
		} else if ("API_REST".equals(userAgent)) {
			session = store.createSession(remoteAddress, principal, completedScheme, "API_REST", "Unknown",
					"Unknown", "Unknown", parameters, configurationService.getIntValue(realm, SESSION_TIMEOUT), realm);
		} else {
			UserstackAgent info;
			try {
				info = lookupUserAgent(userAgent);
				if ("unknown".equals(info.getType())) {

					session = store.createSession(remoteAddress, principal, completedScheme, userAgent, userAgent,
							userAgent, userAgent, parameters, configurationService.getIntValue(realm, SESSION_TIMEOUT), realm);
				} else {
					session = store.createSession(remoteAddress, principal, completedScheme, info.getBrowser().getName(),
							info.getBrowser().getVersion(), info.getOs().getFamily(), info.getName(),
							parameters, configurationService.getIntValue(realm, SESSION_TIMEOUT), realm);
					setCurrentRole(session, permissionService.getPersonalRole(principal));
				}
			} catch (IOException e) {
				session = store.createSession(remoteAddress, principal, completedScheme, 
						StringUtils.defaultIfBlank(StringUtils.substringBefore(userAgent, "/"), "Unknown"),
						StringUtils.defaultIfBlank(StringUtils.substringBefore(StringUtils.substringAfter(userAgent, "/"), " "), "Unknown"),
						"Unknown", "Unknown", parameters, configurationService.getIntValue(realm, SESSION_TIMEOUT), realm);
			}

		}

		
		
		eventService.publishEvent(new SessionOpenEvent(this, session));
		return session;
	}
	
	@Override
	public UserstackAgent lookupUserAgent(String ua) throws IOException {
		
		Cache<String,UserstackAgent> cached = cacheService.getCacheOrCreate(
				"userAgents", String.class, UserstackAgent.class,  
				CreatedExpiryPolicy.factoryOf(Duration.ETERNAL));
		
		UserstackAgent loc = cached.get(ua);
		if(Objects.nonNull(loc)) {
			return loc;
		}
		
		try {
			ObjectMapper o = new ObjectMapper();
			String accessKey = systemConfigurationService.getValue("userstack.accesskey");
			if(StringUtils.isBlank(accessKey)) {
				throw new IOException("No userstack.com access key configured");
			}
			//a9ef4f10d141655e9bbe4ba8cc34316f
			
			String locationJson = httpUtils.doHttpGetContent(
					String.format("http://api.userstack.com/detect?access_key=%s&ua=%s", 
								 accessKey, URLEncoder.encode(ua, "UTF-8")),
							false, 
							new HashMap<String,String>());
			
			UserstackAgent location =  o.readValue(locationJson, UserstackAgent.class);
			cached.put(ua, location);
			return location;
			
		} catch(IllegalStateException e ) {
			throw new IOException("No ipstack.com access key configured");
		}
		
	}
	

	@Override
	public Session getSession(String id) {
		return store.getSessionById(id);
	}

	@Override
	public synchronized boolean isLoggedOn(Session session, boolean touch) {
		if (session == null)
			return false;

		if (session.getSignedOut() == null) {

			if (session.getTimeout() > 0) {
				Calendar currentTime = Calendar.getInstance();
				Calendar c = Calendar.getInstance();
				if (session.getLastUpdated() != null) {
					c.setTime(session.getLastUpdated());
					c.add(Calendar.MINUTE, session.getTimeout());
				}
				if (log.isDebugEnabled()) {
					log.debug("Checking session timeout for " + session.getId() + " currentTime=" + currentTime.getTime() + " lastUpdated="
							+ session.getLastUpdated() + " timeoutThreshold=" + c.getTime());
				}

				if (c.before(currentTime)) {
					if (log.isDebugEnabled()) {
						log.debug("Session has timed out");
					}
					closeSession(session);

					if (log.isDebugEnabled()) {
						log.debug("Session " + session.getPrincipal().getPrincipalName() + "/" + session.getId()
								+ " is now closed");
					}

					return false;
				}
			}
			if (touch) {
				touch(session);
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

		session.setSignedOut(new Date());
		session.setNonCookieKey(null);

		updateSession(session);

		if (!session.isSystem()) {
			eventService.publishEvent(new SessionClosedEvent(this, session));
		}
	}

	@Override
	public void switchRealm(Session session, Realm realm) throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SystemPermission.SYSTEM,
				SystemPermission.SWITCH_REALM);

		if (log.isInfoEnabled()) {
			log.info("Switching " + session.getPrincipal().getName() + " to " + realm.getName() + " realm");
		}

		session.setCurrentRealm(realm);
		updateSession(session);
	}

	protected void assertImpersonationPermission() throws AccessDeniedException {
		assertPermission(UserPermission.IMPERSONATE);
	}

	@Override
	public void switchPrincipal(Session session, Principal principal) throws AccessDeniedException {
		switchPrincipal(session, principal, false);
	}

	@Override
	public void switchPrincipal(Session session, Principal principal, boolean inheritPermissions)
			throws AccessDeniedException {

		assertImpersonationPermission();

		if (log.isInfoEnabled()) {
			log.info("Switching " + session.getPrincipal().getName() + " to " + principal.getName());
		}

		session.setImpersonatedPrincipal(principal);
		session.setInheritPermissions(inheritPermissions);

		session.setCurrentRealm(principal.getRealm());
		setCurrentRole(permissionService.getPersonalRole(principal));
		updateSession(session);
		
		eventService.publishEvent(new UserImpersonatedEvent(this, session, 
				getCurrentRealm(), 
				realmService.getProviderForRealm(getCurrentRealm()), 
				principal, 
				principal.getName())
		);
	}

	@Override
	public Role switchRole(Session session, Long id) throws AccessDeniedException, ResourceNotFoundException {
		try(var c = tryWithElevatedPermissions(RolePermission.READ)) {
			Role role = permissionService.getRoleById(id, getCurrentRealm());
			switchRole(session, role);
			return role;
		} catch(IOException ioe){
			throw new IllegalStateException(ioe);
		}
	}

	@Override
	public void switchRole(Session session, Role role) throws AccessDeniedException {
		if (log.isInfoEnabled()) {
			log.info(String.format("Switching %s role from %s to %s", session.getCurrentPrincipal().getPrincipalName(),
					session.getCurrentRole().getName(), role.getName()));
		}

		setCurrentRole(role);
	}

	@Override
	public void registerResourceSession(Session session, ResourceSession<?> resourceSession) {

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
	public void unregisterResourceSession(Session session, ResourceSession<?> resourceSession) {

		if (resourceSessions.containsKey(session)) {
			resourceSessions.get(session).remove(resourceSession);
		}
	}

	@Override
	public List<Session> getActiveSessions() throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}

		return store.getActiveSessions();
	}
	
	@Override
	public List<Session> getPrincipalActiveSessions(Principal principal) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}

		return store.getPrincipalActiveSessions(principal);
	}
	
	@Override
	public Boolean hasConcurrentSession() throws AccessDeniedException {
		
		var currentSession = getCurrentSession();
		var currentPrincipal = getCurrentPrincipal();
		var currentRealm = getCurrentRealm();
		
		if (currentSession == null) {
			throw new IllegalStateException("Current sesion found to be null cannot proceed.");
		}
		
		var activeSessions = silentlyCallAsSystemContext(() -> {
			return getPrincipalActiveSessions(currentPrincipal);
		}, currentRealm);
		
		var otherActiveSessions = Collections.<Session>emptyList();
		if (activeSessions != null) {
			otherActiveSessions = activeSessions
										.stream()
										.filter(s -> !s.getId().equals(currentSession.getId()))
										.collect(Collectors.toList());
		}
		
		var concurrentSessionDetected = otherActiveSessions != null && !otherActiveSessions.isEmpty();
		
		if (concurrentSessionDetected) {
			
			var sessionTrackedInfo = otherActiveSessions
											.stream()
											.map(s -> s.getId())
											.collect(Collectors.toList())
											.toString();
			
			eventService.publishEvent(new ConcurrentSessionEvent(this, currentSession, sessionTrackedInfo));
		}
		
		return concurrentSessionDetected;
	}

	@Override
	public Long getActiveSessionCount(boolean distinctUsers) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return store.getActiveSessionCount(distinctUsers);
	}

	@Override
	public Long getActiveSessionCount(boolean distinctUsers, Realm realm) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return store.getActiveSessionCount(distinctUsers, realm);
	}

	@Override
	public Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getSessionCount(startDate, endDate, distinctUsers);

	}

	@Override
	public Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers, Realm realm)
			throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getSessionCount(startDate, endDate, distinctUsers, realm);

	}

	@Override
	public Map<String, Long> getBrowserCount(Date startDate, Date endDate) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getBrowserCount(startDate, endDate);

	}

	@Override
	public Map<String, Long> getBrowserCount(Date startDate, Date endDate, Realm realm) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getBrowserCount(startDate, endDate, realm);

	}

	@Override
	public Map<String, Long> getIPCount(Date startDate, Date endDate) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getIPCount(startDate, endDate);

	}

	@Override
	public Map<String, Long> getOSCount(Date startDate, Date endDate) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getOSCount(startDate, endDate);

	}

	@Override
	public Map<String, Long> getOSCount(Date startDate, Date endDate, Realm realm) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getOSCount(startDate, endDate, realm);

	}

	@Override
	public Map<String, Long> getPrincipalUsage(Date startDate, Date endDate) throws AccessDeniedException {

		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			throw new AccessDeniedException();
		}

		return repository.getPrincipalUsage(getCurrentRealm(), 5, startDate, endDate);

	}

	@Override
	public Session getNonCookieSession(String remoteAddr, String token, String authenticationSchemeResourceKey)
			throws AccessDeniedException {

		String sessionId = nonCookieSessions.get(token);
		if(sessionId != null) {
			Session session = getSession(sessionId);
			if (session != null) {
				if (!isLoggedOn(session, true)) {
					throw new AccessDeniedException();
				}
				return session;
			}
		}
		throw new AccessDeniedException();
	}

	@Override
	public void registerNonCookieSession(String remoteAddr, String token, String authenticationSchemeResourceKey,
			Session session) {

		session.setNonCookieKey(token);
		nonCookieSessions.put(token, session.getId());
	}

	@Override
	public void revertPrincipal(Session session) throws AccessDeniedException {

		if (session.getImpersonatedPrincipal() == null) {
			throw new AccessDeniedException("You are not impersonating anyone!");
		}

		if (log.isInfoEnabled()) {
			log.info(
					"Switching " + session.getCurrentPrincipal().getName() + " to " + session.getPrincipal().getName());
		}

		session.setImpersonatedPrincipal(null);
		session.setInheritPermissions(false);

		setCurrentRole(permissionService.getPersonalRole(session.getCurrentPrincipal()));
		updateSession(session);
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {

		runAsSystemContext(() -> {
			if (log.isInfoEnabled()) {
				log.info("Scheduling session reaper job");
			}

			for (Session session : store.getSystemSessions()) {
				if (systemSession != null && systemSession.equals(session)) {
					continue;
				}
				closeSession(session);
			}

			for (Session session : store.getActiveSessions()) {
				if (session.isTransient()) {
					closeSession(session);
				} else if (!session.isSystem()) {
					if (isLoggedOn(session, false)) {
						if (realmService.getRealmPropertyBoolean(session.getPrincipalRealm(),
								"session.closeOnShutdown")) {
							closeSession(session);
							continue;
						}
					}
					notifyReaperListeners(session);
				}
			}

			try {
				if (schedulerService.jobDoesNotExists(SESSION_REAPER_JOB)) {
					schedulerService.scheduleIn(SessionReaperJob.class, SESSION_REAPER_JOB, JobData.of(SESSION_REAPER_JOB), 60000, 60000);
				}
				if (schedulerService.jobDoesNotExists(SESSION_CLEAN_UP_JOB)) {
					schedulerService.scheduleIn(SessionCleanUpJob.class, SESSION_CLEAN_UP_JOB, JobData.of(SESSION_CLEAN_UP_JOB), (int)TimeUnit.DAYS.toMillis(1), (int)TimeUnit.DAYS.toMillis(1));
				}
			} catch (SchedulerException e) {
				log.error("Failed to schedule session reaper job", e);
			}

		});

	}
	
	@Override
	public List<?> searchResourcesWithStateParameters(Realm currentRealm, String searchPattern, int start, int length,
			ColumnSort[] sorting, Set<String> stateParamNames) throws AccessDeniedException {
		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SessionPermission.READ);
		List<Session> sessions = (List<Session>) store.search(currentRealm, searchPattern, start, length, sorting);
		List<SessionWithState> withStates = new ArrayList<>();
		if (sessions != null) {
			for (Session session : sessions) {
				SessionWithState sessionWithState = new SessionWithState();
				Map<String, String> stateMap = new HashMap<>();
				if (stateParamNames != null) {
					for (String param : stateParamNames) {
						String value = session.getStateParameter(param);
						stateMap.put(param, value);
					}
				}
				
				sessionWithState.setId(session.getId());
				sessionWithState.setName(session.getName());
				sessionWithState.setSession(session);
				sessionWithState.setStateMap(stateMap);
				withStates.add(sessionWithState);
			}
		}
		return withStates;
	}

	@Override
	public Long getResourceCount(Realm realm, String searchPattern) throws AccessDeniedException {

		assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SessionPermission.READ);

		return store.getResourceCount(realm, searchPattern);
	}

	@Override
	public void notifyReaperListeners(Session session) {
		synchronized (listeners) {
			for (SessionReaperListener listener : listeners) {
				listener.processSession(session);
			}
		}
	}

	@Override
	public void deleteRealm(Realm realm) {
		repository.deleteRealm(realm);
		if(!store.equals(repository)) {
			store.deleteRealm(realm);
		}
	}

	@Override
	public void cleanUp() throws AccessDeniedException {
		int maxAge = Integer.parseInt(systemConfigurationService.getValue(SESSION_MAX_AGE));
		Calendar maxCal = Calendar.getInstance();
		maxCal.add(Calendar.DAY_OF_YEAR, -maxAge);
		Date maxDate = maxCal.getTime();
		repository.cleanUp(maxDate);
	}
	
	
	@Override
	public Map<String, Long> getSessionGeoInfoByCountryCount() throws AccessDeniedException {
		List<Session> sessions = getActiveSessions();
		Map<String, Long> countMapByCountry = new HashMap<>();
		
		if (sessions != null) {
			for (Session session : sessions) {
				String countryCode = session.getStateParameter(LOCATION_COUNTRY_CODE);
				
				
				if (StringUtils.isNotBlank(countryCode)) {
					String code = countryCode;
				
					Long count = countMapByCountry.get(code);
					if (count == null) {
						count = 0l;
					}
					
					count++;
					
					countMapByCountry.put(code, count);
				}
			}
		}
		
		return countMapByCountry;
	}
	
	@Override
	public Map<String, Long> getSessionGeoInfoByRegionCount(String countryCode) throws AccessDeniedException {
		List<Session> sessions = getActiveSessions();
		Map<String, Long> countMapByRegion = new HashMap<>();
		
		if (sessions != null && StringUtils.isNotBlank(countryCode)) {
			for (Session session : sessions) {
				
				String regionCode = session.getStateParameter(LOCATION_REGION_CODE);
				if (StringUtils.isBlank(regionCode)) {
					continue;
				}
				
				String countryCodeFromSession = session.getStateParameter(LOCATION_COUNTRY_CODE);
				if (!countryCode.equals(countryCodeFromSession)) {
					continue;
				}
				
				String code = String.format("%s-%s", countryCode, regionCode);
				
				Long count = countMapByRegion.get(code);
				if (count == null) {
					count = 0l;
				}
				
				count++;
				
				countMapByRegion.put(code, count);
			}
		}
		
		return countMapByRegion;
	}
	
	private void populateGeoInfoIfEnabled(String remoteAddress, Map<String, String> parameters, Realm realm) {
		if(geoIpService == null)
			return;
		
		if(!geoIpService.isConfigured(realm))
			return;
		
		GeoIPLocation location;
		try {
			location = geoIpService.lookupGeoIP(remoteAddress);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to get location information. Rejecting session.", e);
		}
		
		if (location != null) {
			
			if (StringUtils.isNotBlank(location.getLatitude())) {
				parameters.put(LOCATION_LAT, location.getLatitude());
			}
			
			if (StringUtils.isNotBlank(location.getLongitude())) {
				parameters.put(LOCATION_LON, location.getLongitude());
			}
			
			if (StringUtils.isNotBlank(location.getCountryCode())) {
				parameters.put(LOCATION_COUNTRY_CODE, location.getCountryCode());
			}
			
			if (StringUtils.isNotBlank(location.getRegionCode())) {
				parameters.put(LOCATION_REGION_CODE, location.getRegionCode());
			}
			
		}
	}

	@Override
	public Realm getRealmByHost(String serverName) {
		return realmService.getRealmByHost(serverName);
	}

	@Override
	public void registerCookieDecorator(CookieDecorator decorator) {
		cookieDecorators.add(decorator);
	}

	@Override
	public List<CookieDecorator> getCookieDecorators() {
		return Collections.unmodifiableList(cookieDecorators);
	}

	@Override
	public void touch(Session session) {
		if (session.isReadyForUpdate()) {
			session.updated();
			updateSession(session);
			if (log.isDebugEnabled()) {
				log.debug("Session "
						+ session.getPrincipal().getPrincipalName()
						+ "/" + session.getId()
						+ " state has been updated");
			}
		}
	}

}
