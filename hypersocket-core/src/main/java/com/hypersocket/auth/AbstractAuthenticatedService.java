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
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.utils.HypersocketUtils;

public abstract class AbstractAuthenticatedService implements
		AuthenticatedService {

	@Autowired
	PasswordEncryptionService encryptionService; 

	
	static Logger log = LoggerFactory
			.getLogger(AbstractAuthenticatedService.class);

	static ThreadLocal<Principal> currentPrincipal = new ThreadLocal<Principal>();
	static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	static ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
	static ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();

	public void setCurrentPrincipal(Principal principal, Locale locale,
			Realm realm) {
		currentPrincipal.set(principal);
		currentRealm.set(realm);
		currentLocale.set(locale);
	}
	
	public void setCurrentSession(Session session, Locale locale) {
		if(log.isDebugEnabled()) {
			log.debug("Setting current session context " + session.getId());
		}
		currentPrincipal.set(session.getCurrentPrincipal());
		currentSession.set(session);
		currentRealm.set(session.getCurrentRealm());
		currentLocale.set(locale);
	}

	public Principal getCurrentPrincipal() {
		if(currentPrincipal.get()==null) {
			throw new InvalidAuthenticationContext("No principal is attached to the current context!");
		}
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
	
	@Override
	public String getCurrentPassword() {
		if(currentSession.get()==null) {
			throw new IllegalStateException("Cannot determine current session for getCurrentPassword");
		}
		Session session = currentSession.get();
		try {
			return encryptionService.decrypt(
					session.getStateParameter("password"),
					HypersocketUtils.base64Decode(session.getStateParameter("ss")));
		} catch (Exception e) {
			return "";
		}
	}
	
	@Override
	public void setCurrentPassword(String password) {
		if(currentSession.get()==null) {
			throw new IllegalStateException("Cannot determine current session for setCurrentPassword");
		}
		setCurrentPassword(currentSession.get(), password);
	}
	
	public void setCurrentPassword(Session session, String password) {
		
		try {
			byte[] salt = encryptionService.generateSalt();
			
			session.setStateParameter("password", 
					encryptionService.encrypt(
							password,
							salt));
			
			session.setStateParameter("ss", HypersocketUtils.base64Encode(salt));
		} catch (Exception e) {
			
		}
	}
	
	public boolean hasAuthenticatedContext() {
		return currentPrincipal.get() != null;
	}
	
	public boolean hasSessionContext() {
		return currentSession.get() != null;
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

	protected abstract void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissionTypes)
			throws AccessDeniedException;

}
