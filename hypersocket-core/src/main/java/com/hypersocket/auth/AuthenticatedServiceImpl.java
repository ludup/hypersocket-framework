/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;

public abstract class AuthenticatedServiceImpl extends AbstractAuthenticatedService {

	@Autowired
	protected PermissionService permissionService;
	
	@Override
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions) throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);
	}
	
}
