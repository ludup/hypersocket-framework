/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

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

	static ThreadLocal<Stack<Principal>> currentPrincipal = new ThreadLocal<Stack<Principal>>();
	static ThreadLocal<Stack<Session>> currentSession = new ThreadLocal<Stack<Session>>();
	static ThreadLocal<Stack<Realm>> currentRealm = new ThreadLocal<Stack<Realm>>();
	static ThreadLocal<Stack<Locale>> currentLocale = new ThreadLocal<Stack<Locale>>();
		
	static ThreadLocal<Boolean> isDelayingEvents = new ThreadLocal<Boolean>();
	static ThreadLocal<LinkedList<SystemEvent>> delayedEvents = new ThreadLocal<LinkedList<SystemEvent>>();
	
	static ThreadLocal<Stack<Set<PermissionType>>> elevatedPermissions = new ThreadLocal<Stack<Set<PermissionType>>>();
	
	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException;
	
	
	@Override
	public void elevatePermissions(PermissionType... permissions) {
		if(elevatedPermissions.get()==null) {
			throw new IllegalStateException("No session in context to elevate permissions on");
		}
		Set<PermissionType> elevated = elevatedPermissions.get().peek();
		elevated.addAll(Arrays.asList(permissions));
	}
	
	@Override
	public void clearElevatedPermissions() {
		if(elevatedPermissions.get()==null) {
			throw new IllegalStateException("No session in context to elevate permissions on");
		}
		elevatedPermissions.get().peek().clear();
	}
	
	protected Set<PermissionType> getElevatedPermissions() {
		if(elevatedPermissions.get()==null) {
			throw new IllegalStateException("No session in context to elevate permissions on");
		}
		return elevatedPermissions.get().peek();
	}
	
	protected boolean hasElevatedPermissions() {
		return elevatedPermissions.get()!=null && !elevatedPermissions.get().isEmpty();
	}
	
	@Override
	public void setCurrentSession(Session session, Locale locale) {
		setCurrentSession(session, session.getCurrentRealm(), locale);
	}
	
	@Override
	public void setCurrentSession(Session session, Realm realm, Locale locale) {
		setCurrentSession(session, realm, session.getCurrentPrincipal(), locale);
	}
	
	@Override
	public void setCurrentSession(Session session, Realm realm, Principal principal, Locale locale) {
		if(log.isDebugEnabled()) {
			log.debug("Setting current session context " + session.getId());
		}
		if(session.getCurrentPrincipal()==null) {
			throw new InvalidAuthenticationContext("Session does not have a current principal!");
		}
		if(currentSession.get()==null) {
			currentSession.set(new Stack<Session>());
			currentPrincipal.set(new Stack<Principal>());
			currentRealm.set(new Stack<Realm>());
			currentLocale.set(new Stack<Locale>());
			elevatedPermissions.set(new Stack<Set<PermissionType>>());
		}
		currentPrincipal.get().push(principal);
		currentSession.get().push(session);
		currentRealm.get().push(realm);
		currentLocale.get().push(locale);
		elevatedPermissions.get().push(new HashSet<PermissionType>());
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("There are now %d context references", currentSession.get().size()));
		}
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("Context realm=%s principal=%s session=%s", getCurrentRealm().getName(),
					getCurrentPrincipal().getName(), getCurrentSession().getId()));
		}
	}
	

	@Override
	public Principal getCurrentPrincipal() {
		if(currentPrincipal.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentPrincipal.get().peek();
	}
	
	@Override
	public Session getCurrentSession() throws InvalidAuthenticationContext {
		if(currentSession.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentSession.get().peek();
	}

	@Override
	public Locale getCurrentLocale() {
		if(currentLocale.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentLocale.get().peek();
	}
	
	@Override
	public Realm getCurrentRealm() {
		if(currentRealm.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentRealm.get().peek();
	}

	@Override
	public void clearPrincipalContext() {
		
		if(currentSession.get() != null) {
			currentSession.get().pop();
			currentPrincipal.get().pop();
			currentLocale.get().pop();
			currentRealm.get().pop();
			elevatedPermissions.get().pop();
			if(currentSession.get().size() > 0) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("There are %d context references left", currentSession.get().size()));
				}
				return;
			}
		}
		if(log.isDebugEnabled()) {
			log.debug(String.format("There are no context references left"));
		}
		currentLocale.remove();
		currentPrincipal.remove();
		currentSession.remove();
		currentRealm.remove();
		elevatedPermissions.remove();
	}
	
	@Override
	public String getCurrentUsername() {
		return getCurrentPrincipal().getPrincipalName();
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
