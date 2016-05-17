/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;

public abstract class PasswordEnabledAuthenticatedServiceImpl 
			extends AuthenticatedServiceImpl 
			implements PasswordEnabledAuthenticatedService {

	@Autowired
	EncryptionService encryptionService; 
	
	@Autowired
	protected PermissionService permissionService;
	
	static Logger log = LoggerFactory
			.getLogger(PasswordEnabledAuthenticatedServiceImpl.class);
	
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);
	}
	
	@Override
	public String getCurrentPassword() {
		if(currentSession.get()==null) {
			throw new IllegalStateException("Cannot determine current session for getCurrentPassword");
		}
		Session session = currentSession.get().peek();
		try {
			return encryptionService.decryptString("sessionState",
					session.getStateParameter("password"),
					session.getPrincipalRealm());
		} catch (Exception e) {
			log.error("Failed to get session state", e);
			return "";
		}
	}
	
	@Override
	public void setCurrentPassword(String password) {
		if(currentSession.get()==null) {
			throw new IllegalStateException("Cannot determine current session for setCurrentPassword");
		}
		setCurrentPassword(currentSession.get().peek(), password);
	}
	
	public void setCurrentPassword(Session session, String password) {
		
		try {
			session.setStateParameter("password", 
					encryptionService.encryptString("sessionState",
							password,
							session.getPrincipalRealm()));
		} catch (Exception e) {
			log.error("Failed to store session state", e);
		}
	}

}
