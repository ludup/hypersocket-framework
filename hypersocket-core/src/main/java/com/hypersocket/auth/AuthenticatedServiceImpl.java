/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public abstract class AuthenticatedServiceImpl implements AuthenticatedService {

	static Logger log = LoggerFactory.getLogger(AuthenticatedServiceImpl.class);

	static ThreadLocal<Principal> currentPrincipal = new ThreadLocal<Principal>();
	static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	static ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
	static ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();
	static ThreadLocal<Integer> currentReferences = new ThreadLocal<Integer>();
	
	static ThreadLocal<Boolean> isDelayingEvents = new ThreadLocal<Boolean>();
	static ThreadLocal<LinkedList<SystemEvent>> delayedEvents = new ThreadLocal<LinkedList<SystemEvent>>();
	
	static ThreadLocal<List<PermissionType>> elevatedPermissions = new ThreadLocal<List<PermissionType>>();
	
	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException;
	
	
	@Override
	public void elevatePermissions(PermissionType... permissions) {
		elevatedPermissions.set(Arrays.asList(permissions));
	}
	
	@Override
	public void clearElevatedPermissions() {
		elevatedPermissions.remove();
	}
	
	protected List<PermissionType> getElevatedPermissions() {
		return elevatedPermissions.get();
	}
	
	protected boolean hasElevatedPermissions() {
		return elevatedPermissions.get()!=null;
	}
	
	@Override
	public void setCurrentPrincipal(Principal principal, Locale locale,
			Realm realm) {
		currentPrincipal.set(principal);
		currentRealm.set(realm);
		currentLocale.set(locale);
		
		if(currentReferences.get()==null) {
			currentReferences.set(new Integer(1));
		} else {
			currentReferences.set(currentReferences.get() + 1);
		}
		if(log.isDebugEnabled()) {
			log.debug(String.format("There are now %d context references", currentReferences.get()));
		}
		if(log.isInfoEnabled()) {
			log.info(String.format("Context realm=%s prinipal=%s", getCurrentRealm().getName(),
					getCurrentPrincipal().getName()));
		}
	}
	
	@Override
	public void setCurrentSession(Session session, Locale locale) {
		if(log.isDebugEnabled()) {
			log.debug("Setting current session context " + session.getId());
		}
		if(session.getCurrentPrincipal()==null) {
			throw new InvalidAuthenticationContext("Session does not have a current principal!");
		}
		currentPrincipal.set(session.getCurrentPrincipal());
		currentSession.set(session);
		currentRealm.set(session.getCurrentRealm());
		currentLocale.set(locale);
		
		if(currentReferences.get()==null) {
			currentReferences.set(new Integer(1));
		} else {
			currentReferences.set(currentReferences.get() + 1);
		}
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("There are now %d context references", currentReferences.get()));
		}
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Context realm=%s principal=%s session=%s", getCurrentRealm().getName(),
					getCurrentPrincipal().getName(), getCurrentSession().getId()));
		}
	}
	
	@Override
	public void setCurrentRealm(Realm realm) {
		currentRealm.set(realm);

		if(log.isInfoEnabled()) {
			log.info(String.format("Context realm=%s principal=/%s", getCurrentRealm().getName(),
					getCurrentPrincipal().getName()));
		}
	}

	@Override
	public Principal getCurrentPrincipal() {
		if(currentPrincipal.get()==null) {
			return getCurrentSession().getCurrentPrincipal();
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
		if(currentRealm.get()!=null) {
			return currentRealm.get();
		}
		
		return getCurrentSession().getCurrentRealm();
	}

	@Override
	public void clearPrincipalContext() {
		
		if(currentReferences.get() > 1) {
			if(log.isDebugEnabled()) {
				log.debug("Clearing authenticated context reference.");
			}
			currentReferences.set(currentReferences.get() - 1);
			if(log.isDebugEnabled()) {
				log.debug(String.format("There are %d context references left", currentReferences.get()));
			}
			return;
		}
		if(log.isDebugEnabled()) {
			log.debug(String.format("There are no context references left"));
		}
		currentLocale.set(null);
		currentPrincipal.set(null);
		currentSession.set(null);
		currentRealm.set(null);
		currentReferences.set(null);
		elevatedPermissions.set(null);
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
