/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.PasswordEnabledAuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface SessionService extends PasswordEnabledAuthenticatedService {

	static final String RESOURCE_BUNDLE = "SessionService";

	boolean isLoggedOn(Session session, boolean touch);

	Session getSession(String id);

	Session openSession(String remoteAddress, Principal principal,
			AuthenticationScheme completedScheme, String userAgent,
			Map<String, String> parameters);

	Session openSession(String remoteAddress, Principal principal,
			AuthenticationScheme completedScheme, String userAgent,
			Map<String, String> parameters, Realm realm);
	
	void closeSession(Session session);

	void switchRealm(Session session, Realm realm) throws AccessDeniedException;

	void registerResourceSession(Session session,
			ResourceSession<?> resourceSession);

	boolean hasResourceSession(Session session, Resource resource);

	void unregisterResourceSession(Session session,
			ResourceSession<?> resourceSession);

	List<Session> getActiveSessions() throws AccessDeniedException;

	<T> SessionResourceToken<T> createSessionToken(T resource);

	<T> SessionResourceToken<T> getSessionToken(String shortCode,
			Class<T> resourceClz);

	<T> T getSessionTokenResource(String shortCode,
			Class<T> resourceClz);
	
	Session getNonCookieSession(String remoteAddr, String requestHeader,
			String authenticationSchemeResourceKey)
			throws AccessDeniedException;

	void registerNonCookieSession(String remoteAddr, String requestHeader,
			String authenticationSchemeResourceKey, Session session);

	void switchPrincipal(Session session, Principal principal)
			throws AccessDeniedException;
	
	void revertPrincipal(Session session)
			throws AccessDeniedException;

	void switchPrincipal(Session session, Principal principal,
			boolean inheritPermissions) throws AccessDeniedException;

	Session getSystemSession();

	Map<String, Long> getBrowserCount(Date startDate, Date endDate)
			throws AccessDeniedException;
	
	Map<String, Long> getBrowserCount(Date startDate, Date endDate, Realm realm)
			throws AccessDeniedException;

	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers)
			throws AccessDeniedException;
	
	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers, Realm realm)
			throws AccessDeniedException;

	Long getActiveSessionCount(boolean distinctUsers)
			throws AccessDeniedException;
	
	Long getActiveSessionCount(boolean distinctUsers, Realm realm)
			throws AccessDeniedException;

	Map<String, Long> getOSCount(Date startDate, Date endDate)
			throws AccessDeniedException;
	
	Map<String, Long> getOSCount(Date startDate, Date endDate, Realm realm)
			throws AccessDeniedException;

	Map<String, Long> getIPCount(Date startDate, Date endDate)
			throws AccessDeniedException;

	List<?> searchResources(Realm currentRealm, String searchPattern, int start, int length, ColumnSort[] sorting) throws AccessDeniedException;
	
	List<?> searchResourcesWithStateParameters(Realm currentRealm, String searchPattern, int start, int length, ColumnSort[] sorting, Set<String> stateParamNames) throws AccessDeniedException;

	Long getResourceCount(Realm currentRealm, String searchPattern) throws AccessDeniedException;

	Map<String, Long> getPrincipalUsage(Date from, Date now) throws AccessDeniedException;

	Role switchRole(Session session, Long id) throws AccessDeniedException, ResourceNotFoundException;

	void switchRole(Session currentSession, Role role) throws AccessDeniedException;

	Role getCurrentRole(Session session);
	
	void updateSession(Session session);

	void notifyReaperListeners(Session session);

	void registerReaperListener(SessionReaperListener listener);

	void deleteRealm(Realm realm);
	
	void cleanUp() throws AccessDeniedException;
	
	Map<String, Long> getSessionGeoInfoByCountryCount() throws AccessDeniedException;
	 
	Map<String, Long> getSessionGeoInfoByRegionCount(String countryCode) throws AccessDeniedException;
	
	IStackLocation lookupGeoIP(Realm realm, String ipAddress);
	
	boolean isIpStackAPIKeySet(Realm realm);

	UserstackAgent lookupUserAgent(String ua) throws IOException;

	Realm getRealmByHost(String serverName); 
}

