/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

public abstract class AuthenticatedServiceImpl extends AbstractAuthenticatedService {

	@Autowired
	protected PermissionService permissionService;
	
	@Autowired
	@Qualifier("transactionManager")
	PlatformTransactionManager txManager;
	
	@Override
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);
	}
	
	protected <T> T doInTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException {
		
		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		
		try {
			return tmpl.execute(transaction);
		} catch (Throwable e) {
			if(e.getCause() instanceof ResourceChangeException) {
				throw (ResourceChangeException) e.getCause();
			} else if(e.getCause() instanceof ResourceCreationException) {
				throw (ResourceCreationException) e.getCause();
			} else if(e.getCause() instanceof ResourceNotFoundException) {
				throw (ResourceNotFoundException) e.getCause();
			} else if(e.getCause() instanceof AccessDeniedException) {
				throw (AccessDeniedException) e.getCause();
			}
			throw new ResourceException(AuthenticationService.RESOURCE_BUNDLE, "error.transactionFailed", e.getMessage());
		}
		
		
	}
	

}
