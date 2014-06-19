/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.List;
import java.util.Set;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface PermissionService extends AuthenticatedService {

	static final String RESOURCE_BUNDLE = "PermissionService";
	
	public PermissionCategory registerPermissionCategory(String resourceBundle, String resourceKey);

	public Permission registerPermission(String resourceKey, PermissionCategory category);

	public Permission registerPermission(String resourceKey,
			PermissionCategory category, boolean hidden);
	
	public Permission getPermission(String resourceKey);
	
	public Role createRole(String name, Realm realm) throws AccessDeniedException;
	
	void assignRole(Role role, Principal principal) throws AccessDeniedException;
	
	void verifyPermission(Principal principal, PermissionStrategy strategy, PermissionType... permission) throws AccessDeniedException;

	Set<Principal> getUsersWithPermissions(PermissionType permissions);

	Role getRole(String name, Realm realm) throws ResourceNotFoundException;

	void deleteRole(Role name) throws ResourceChangeException, AccessDeniedException;

	List<Role> allRoles(Realm realm);

	public List<Permission> allPermissions();

	Role createRole(String name, Realm realm, List<Principal> principals,
			List<Permission> permissions) throws AccessDeniedException;

	Role updateRole(Role role, String name,
			List<Principal> principals, List<Permission> permissions) throws AccessDeniedException;

	public Role getRoleById(Long id, Realm realm) throws ResourceNotFoundException;

	public Permission getPermissionById(Long perm);

	Set<Permission> getPrincipalPermissions(Principal principal);

	boolean hasSystemPermission(Principal principal);

	public Long getRoleCount(String searchPattern) throws AccessDeniedException;

	public List<?> getRoles(String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	Role getPersonalRole(Principal principal);

}
