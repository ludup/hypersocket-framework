package com.hypersocket.auth;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class AbstractAuthenticatedServiceImpl extends AuthenticatedServiceImpl {

	@Autowired
	protected PermissionService permissionService; 
	
	@Autowired
	SessionService sessionService;

	@Override
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);
	}
	
	public Session getSystemSession() {
		return sessionService.getSystemSession();
	}

}
