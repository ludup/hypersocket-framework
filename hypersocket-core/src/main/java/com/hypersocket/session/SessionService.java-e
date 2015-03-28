/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.PasswordEnabledAuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;

public interface SessionService extends PasswordEnabledAuthenticatedService {

	static final String RESOURCE_BUNDLE = "SessionService";

	boolean isLoggedOn(Session session, boolean touch);

	Session getSession(String id);

	Session openSession(String remoteAddress, Principal principal,
			AuthenticationScheme completedScheme, String userAgent,
			Map<String, String> parameters);

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
}
