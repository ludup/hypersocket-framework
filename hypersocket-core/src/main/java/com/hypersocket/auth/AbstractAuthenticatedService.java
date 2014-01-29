/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public abstract class AbstractAuthenticatedService implements
		AuthenticatedService {

	static Logger log = LoggerFactory
			.getLogger(AbstractAuthenticatedService.class);

	ThreadLocal<Principal> currentPrincipal = new ThreadLocal<Principal>();
	ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
	ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();

	public void setCurrentPrincipal(Principal principal, Locale locale,
			Realm realm) {
		currentPrincipal.set(principal);
		currentRealm.set(realm);
		currentLocale.set(locale);
	}
	
	public void setCurrentSession(Session session, Locale locale) {
		currentPrincipal.set(session.getPrincipal());
		currentSession.set(session);
		currentRealm.set(session.getCurrentRealm());
		currentLocale.set(locale);
	}

	public Principal getCurrentPrincipal() {
		return currentPrincipal.get();
	}
	
	public Session getCurrentSession() throws InvalidAuthenticationContext {
		if(currentSession.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentSession.get();
	}

	public Realm getCurrentRealm() {
		return currentRealm.get();
	}

	public Locale getCurrentLocale() {
		return currentLocale.get();
	}

	public void clearPrincipalContext() {
		currentLocale.set(null);
		currentPrincipal.set(null);
	}

	public boolean hasAuthenticatedContext() {
		return currentPrincipal.get() != null;
	}

	protected void assertPermission(PermissionType... permission)
			throws AccessDeniedException {
		assertPermission(PermissionStrategy.REQUIRE_ALL_PERMISSIONS, permission);
	}

	protected void assertAllPermission(PermissionType... permission)
			throws AccessDeniedException {
		assertPermission(PermissionStrategy.REQUIRE_ALL_PERMISSIONS, permission);
	}

	protected void assertAnyPermission(PermissionType... permission)
			throws AccessDeniedException {
		assertPermission(PermissionStrategy.REQUIRE_ANY, permission);
	}

	protected void assertPermission(PermissionStrategy strategy,
			PermissionType... permissions) throws AccessDeniedException {

		if (log.isWarnEnabled() && !hasAuthenticatedContext()) {
			log.warn("Permission " + permissions[0].getResourceKey()
					+ " is being asserted without a principal in context");
		}

		verifyPermission(getCurrentPrincipal(), strategy, permissions);
	}

	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissionTypes)
			throws AccessDeniedException;

}
