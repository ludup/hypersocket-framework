/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionScope;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class AuthenticatedServiceImpl implements AuthenticatedService {

	static Logger log = LoggerFactory.getLogger(AuthenticatedServiceImpl.class);

	static ThreadLocal<Stack<Principal>> currentPrincipal = new ThreadLocal<Stack<Principal>>();
	static ThreadLocal<Stack<Session>> currentSession = new ThreadLocal<Stack<Session>>();
	static ThreadLocal<Stack<Realm>> currentRealm = new ThreadLocal<Stack<Realm>>();
	static ThreadLocal<Stack<Locale>> currentLocale = new ThreadLocal<Stack<Locale>>();
	static Map<Session,Role> currentRole = new HashMap<Session,Role>();
	
	static ThreadLocal<Boolean> isDelayingEvents = new ThreadLocal<Boolean>();
	static ThreadLocal<LinkedList<SystemEvent>> delayedEvents = new ThreadLocal<LinkedList<SystemEvent>>();
	
	static ThreadLocal<Stack<Set<PermissionType>>> elevatedPermissions = new ThreadLocal<Stack<Set<PermissionType>>>();
	
	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException;
	
	protected abstract Role getPersonalRole(Principal principal) throws AccessDeniedException;
	
	@Autowired
	private SessionService sessionService;  

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
	
	protected boolean isDelegatedRealm() {
		return !getCurrentRealm().equals(getCurrentPrincipal().getRealm());
	}
	
	@Override
	public void setupSystemContext() {
		setCurrentSession(sessionService.getSystemSession(), Locale.getDefault());
	}
	
	@Override
	public void setupSystemContext(Realm realm) {
		setCurrentSession(sessionService.getSystemSession(), realm, Locale.getDefault());
	}
	
	@Override
	public void setupSystemContext(Principal principal) {
		if(Objects.isNull(principal)) {
			throw new IllegalStateException("Principal object cannot be null when starting a System context");
		}
		setCurrentSession(sessionService.getSystemSession(), principal.getRealm(), principal, Locale.getDefault());
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
		if(currentRole.containsKey(session)) {
			currentRole.put(session, ApplicationContextServiceImpl.getInstance().getBean(
					PermissionService.class).getPersonalRole(principal));
		}
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
		Principal principal = currentPrincipal.get().peek();
		if(log.isDebugEnabled()) {
			log.debug(String.format("Current principal is %s", principal.getPrincipalName()));
		}
		return principal;
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
	public Realm getCurrentRealm(Principal principal) {
		if(currentRealm.get()==null) {
			return principal.getRealm();
		}
		return currentRealm.get().peek();
	}
	
	@Override
	public Realm getCurrentRealm() {
		if(currentRealm.get()==null) {
			throw new InvalidAuthenticationContext("No session is attached to the current context!");
		}
		return currentRealm.get().peek();
	}
	
	@Override
	public Role getCurrentRole() {
		return currentRole.get(getCurrentSession());
	}
	
	public Role getCurrentRole(Session session) {
		if(!currentRole.containsKey(session)) {
			currentRole.put(session, ApplicationContextServiceImpl.getInstance().getBean(
					PermissionService.class).getPersonalRole(session.getCurrentPrincipal()));
		}
		return currentRole.get(session);
	}
	@Override
	public void setCurrentRole(Session session, Role role) {
		currentRole.put(session, role);
	}
	
	@Override
	public void setCurrentRole(Role role) {
		currentRole.put(getCurrentSession(), role);
	}
	
	public void closeSession(Session session) {
		currentRole.remove(session);
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
	
	protected abstract Set<Role> getCurrentRoles();
	
	protected void assertPermissionOrRole(PermissionScope scope, PermissionType permission, Role... roles)
			throws AccessDeniedException {
		Set<Role> principalRoles = getCurrentRoles();
		for(Role role : roles) {
			if(principalRoles.contains(role)) {
				return;
			}
		}
		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
	}
	
	protected void assertAdministrativePermission() throws AccessDeniedException {

		if(hasAdministrativePermission(getCurrentPrincipal())) {
			return;
		}
		
		throw new AccessDeniedException();
	}
	
	protected abstract boolean hasAdministrativePermission(Principal principal);
	
	protected void assertAnyPermissionOrRealmAdministrator(PermissionScope scope, PermissionType... permission)
			throws AccessDeniedException {
		Realm parentRealm = getCurrentPrincipal().getRealm();
		Realm assertRealm = getCurrentRealm();
		
		if(scope == PermissionScope.INCLUDE_CHILD_REALMS) {
			while(!parentRealm.equals(assertRealm) && assertRealm.hasParent()) {
				assertRealm = assertRealm.getParent();
			}
		}
		if(!parentRealm.equals(assertRealm)) {
			assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
		}
		
		for(Role role : getCurrentRoles()) {
			if(role.isSystem() && role.isAllPermissions()) {
				return;
			}
		}
		
		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
	}
	
	protected void assertRealmAdministrator(PermissionScope scope)
			throws AccessDeniedException {
		Realm parentRealm = getCurrentPrincipal().getRealm();
		Realm assertRealm = getCurrentRealm();
		
		if(scope==PermissionScope.INCLUDE_CHILD_REALMS) {
			while(!parentRealm.equals(assertRealm) && assertRealm.hasParent()) {
				assertRealm = assertRealm.getParent();
			}
		}
		
		if(!parentRealm.equals(assertRealm)) {
			throw new AccessDeniedException();
		}
		
		for(Role role : getCurrentRoles()) {
			if(role.isSystem() && role.isAllPermissions()) {
				return;
			}
		}
		
		throw new AccessDeniedException();
	}
	
	protected void assertRole(Role... roles) throws AccessDeniedException {
		if(hasAdministrativePermission(getCurrentPrincipal())) {
			return;
		}
		Set<Role> principalRoles = getCurrentRoles();
		for(Role role : roles) {
			if(principalRoles.contains(role)) {
				return;
			}
		}
		throw new AccessDeniedException("User is not a member of " + ResourceUtils.createCommaSeparatedString(Arrays.asList(roles)) + " in ");
	}
	
	protected void assertRoleOrAnyPermission(Role role, PermissionType... permission)
			throws AccessDeniedException {
		Set<Role> principalRoles = getCurrentRoles();
		if(principalRoles.contains(role)) {
			return;
		}
		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, permission);
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

		if(permissions.length > 0) {
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
		}
		
		verifyPermission(getCurrentPrincipal(), strategy, permissions);
	}

	@Override
	public boolean hasAuthenticatedContext() {
		return currentPrincipal.get() != null;
	}
	
	@Override 
	public boolean hasSystemContext() {
		try {
			Principal principal = getCurrentPrincipal();
			return principal.isSystem() && principal.getType() == PrincipalType.SYSTEM;
		} catch (InvalidAuthenticationContext e) {
			return false;
		}
	}
	
	@Override
	public boolean hasSessionContext() {
		return currentSession.get() != null;
	}

}
