package com.hypersocket.auth;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;

public abstract class AbstractAuthenticatedServiceImpl extends AuthenticatedServiceImpl {

	@Autowired
	protected PermissionService permissionService; 
	
	@Override
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);

	}

}
