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
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.tables.ColumnSort;

public interface PermissionService extends AuthenticatedService {

	static final String RESOURCE_BUNDLE = "PermissionService";
	static final String ROLE_ADMINISTRATOR = "Administrator";
	static final String ROLE_EVERYONE = "Everyone";

	public PermissionCategory registerPermissionCategory(String resourceBundle, String resourceKey);

	public Permission getPermission(String resourceKey);

	public Role createRole(String name, Realm realm) throws AccessDeniedException, ResourceCreationException;

	public void unassignRole(Role customerRole, Principal principal) throws AccessDeniedException;
	
	public void unassignRole(Role customerRole, Principal... principals) throws AccessDeniedException;
	
	void assignRole(Role role, Principal principal) throws AccessDeniedException;

	void assignRole(Role role, Principal... principals) throws AccessDeniedException;

	void verifyPermission(Principal principal, PermissionStrategy strategy, PermissionType... permission)
			throws AccessDeniedException;

	Set<Principal> getUsersWithPermissions(PermissionType permissions);

	Role getRole(String name, Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	void deleteRole(Role name) throws ResourceChangeException, AccessDeniedException;

	List<Role> allRoles(Realm realm) throws AccessDeniedException;

	public List<Permission> allPermissions();

	Role createRole(String name, Realm realm, List<Principal> principals, List<Permission> permissions)
			throws AccessDeniedException, ResourceCreationException;

	Role updateRole(Role role, String name, List<Principal> principals, List<Permission> permissions)
			throws AccessDeniedException, ResourceChangeException;

	public Role getRoleById(Long id, Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	public Permission getPermissionById(Long perm);

	Set<Permission> getPrincipalPermissions(Principal principal) throws AccessDeniedException;

	boolean hasSystemPermission(Principal principal);

	public Long getRoleCount(String searchPattern) throws AccessDeniedException;

	public List<?> getRoles(String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException;

	Role getPersonalRole(Principal principal);

	Set<Role> getPrincipalRoles(Principal principal) throws AccessDeniedException;

	List<PropertyCategory> getRoleTemplates() throws AccessDeniedException;

	Permission registerPermission(PermissionType type, PermissionCategory category);

	void grantPermission(Role everyone, Permission permission) throws AccessDeniedException, ResourceChangeException;

	void revokePermissions(Principal principal, @SuppressWarnings("unchecked") TransactionAdapter<Principal>... ops)
			throws ResourceException, AccessDeniedException;

	boolean hasPermission(Principal principal, Permission permission);

	public boolean hasRole(Principal principal, Role customerRole) throws AccessDeniedException;

	public void assertResourceAccess(AssignableResource resource, Principal principal) throws AccessDeniedException;
}
