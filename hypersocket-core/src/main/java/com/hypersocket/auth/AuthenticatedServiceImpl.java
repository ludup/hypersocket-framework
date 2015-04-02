/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.LinkedList;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.Session;

public abstract class AuthenticatedServiceImpl implements AuthenticatedService {

	static Logger log = LoggerFactory.getLogger(AuthenticatedServiceImpl.class);

	static ThreadLocal<Principal> currentPrincipal = new ThreadLocal<Principal>();
	static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	static ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
	static ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();

	static ThreadLocal<Boolean> isDelayingEvents = new ThreadLocal<Boolean>();
	static ThreadLocal<LinkedList<SystemEvent>> delayedEvents = new ThreadLocal<LinkedList<SystemEvent>>();
	
	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException;
	
	@Override
	public void setCurrentPrincipal(Principal principal, Locale locale,
			Realm realm) {
		currentPrincipal.set(principal);
		currentRealm.set(realm);
		currentLocale.set(locale);
	}
	
	@Override
	public void setCurrentSession(Session session, Locale locale) {
		if(log.isDebugEnabled()) {
			log.debug("Setting current session context " + session.getId());
		}
		currentPrincipal.set(session.getCurrentPrincipal());
		currentSession.set(session);
		currentRealm.set(session.getCurrentRealm());
		currentLocale.set(locale);
	}

	@Override
	public Principal getCurrentPrincipal() {
		if(currentPrincipal.get()==null) {
			throw new InvalidAuthenticationContext("No principal is attached to the current context!");
		}
		return currentPrincipal.get();
	}
	
	@Override
	public Session getCurrentSession() throws InvalidAuthenticationContext {
		if(currentSession.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentSession.get();
	}

	@Override
	public Locale getCurrentLocale() {
		return currentLocale.get();
	}
	
	@Override
	public Realm getCurrentRealm() {
		if(currentSession.get()==null) {
			return currentRealm.get();
		}
		return getCurrentSession().getCurrentRealm();
	}

	@Override
	public void clearPrincipalContext() {
		if(log.isDebugEnabled()) {
			log.debug("Clearing authenticated context.");
		}
		currentLocale.set(null);
		currentPrincipal.set(null);
		currentSession.set(null);
		currentRealm.set(null);
	}
	
	@Override
	public String getCurrentUsername() {
		if(currentPrincipal.get()==null) {
			throw new IllegalStateException("Cannot determine current principal for getCurrentUsername");
		}
		return currentPrincipal.get().getPrincipalName();
	}
	
	protected void assertPermission(PermissionType permission)
			throws AccessDeniedException {
		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
	}

	protected void assertAnyPermission(PermissionType... permission)
			throws AccessDeniedException {
		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
	}

	protected void assertAnyPermission(PermissionStrategy strategy,
			PermissionType... permissions) throws AccessDeniedException {

		if (log.isWarnEnabled() && !hasAuthenticatedContext()) {
			log.warn("Permission " + permissions[0].getResourceKey()
					+ " is being asserted without a principal in context");
		}

		if(hasSessionContext()) {
			if(getCurrentSession().isImpersonating() && getCurrentSession().isInheritPermissions()) {
				verifyPermission(getCurrentSession().getInheritedPrincipal(), strategy, permissions);
				return;
			}
		}
		
		verifyPermission(getCurrentPrincipal(), strategy, permissions);
	}

	@Override
	public boolean hasAuthenticatedContext() {
		return currentPrincipal.get() != null;
	}
	
	@Override
	public boolean hasSessionContext() {
		return currentSession.get() != null;
	}

}
