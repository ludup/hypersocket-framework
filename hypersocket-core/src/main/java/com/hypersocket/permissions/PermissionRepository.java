/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tables.ColumnSort;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionRepository extends AbstractResourceRepository<Role> {

	public PermissionCategory createCategory(String resourceBundle,
			String resourceKey);

	public Permission createPermission(String resourceKey, boolean system,
			PermissionCategory category, boolean hidden);

	public Role createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system, RoleType type);

	public PermissionCategory getCategoryById(Long id);

	public PermissionCategory getCategoryByKey(String resourceBundle,
			String resourceKey);

	public Permission getPermissionById(Long id);

	public Permission getPermissionByResourceKey(String name);

	public Role getRoleById(Long id);

	public Role getRoleByName(String name, Realm realm);

	public void grantPermission(Role role, Permission permissions);

	public void revokePermission(Role role, Permission permissions);

	public void grantPermissions(Role role, Collection<Permission> permissions);

	public void revokePermission(Role role, Collection<Permission> permissions);

	public void deleteCategory(PermissionCategory category);

	public void deletePermission(Permission permission);

	public void deleteRole(Role role);

	public List<PermissionCategory> getAllCatgories();

	public List<Permission> getAllPermissions(Set<Long> registered,
			boolean includeSystem);

	public void assignRole(Role role, Principal... principal);

	public void unassignRole(Role role, Principal... principal);

	Set<Permission> getDelegatedPrincipalPermissions(Realm currentRealm,
			Collection<Principal> principals,
			PermissionType... permissionTypes);

	Set<Principal> getPrincipalsWithPermissions(PermissionType permissions);

	public List<Role> getRolesForRealm(Realm realm);

	public List<Permission> getPermissionsByCategory(PermissionCategory cat1);

	void saveRole(Role role);

	Set<Principal> getPrincipalsWithPermissions(Permission permission);

	Set<Role> getRolesWithPermissions(PermissionType permission);

	Set<Role> getRolesWithPermissions(Permission permission);

	List<Role> searchRoles(Realm realm, String searchPattern, String searchColumn, int start,
			int length, ColumnSort[] sorting);

	Long countRoles(Realm realm, String searchPattern, String searchColumn);

	Role getPersonalRole(Principal principal);

	Role createRole(String name, Realm realm, RoleType type);

	Set<Role> getRolesForPrincipal(List<Principal> associatedPrincipals);
	
	Collection<Principal> getPrincpalsByRole(Realm realm, Collection<Role> associatedPrincipals);

	Set<Role> getAllUserRoles(Realm realm);

	void saveRole(Role role, Realm realm, Principal[] principals,
			Collection<Permission> permissions, Map<String,String> properties,
			@SuppressWarnings("unchecked") TransactionOperation<Role>... ops) throws ResourceException;

	void updateRole(Role role, Set<Principal> unassignPrincipals,
			Set<Principal> assignPrincipals, Set<Permission> revokePermissions,
			Set<Permission> grantPermissions, Map<String,String> properties,
			@SuppressWarnings("unchecked") TransactionOperation<Role>... ops) throws ResourceException;

	void createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system,
			Set<Permission> permissions, Map<String,String> properties, RoleType type) throws ResourceException;

	Role getPersonalRole(Principal principal, boolean createIfNotFound);

	long getAssignableResourceCount(Principal principal);

	List<Permission> getPermissionsByResourceKeys(final String...resourceKeys);

	List<Role> searchNoPersonalNoAllUserRoles(final Realm realm, String searchPattern,
													 int start, int length, ColumnSort[] sorting);
	List<Role> getPermissionsByIds(Long...ids);

	Permission updatePermission(String name, boolean system, PermissionCategory category, boolean hidden);

	void deleteRealm(Realm realm);

	Role getRoleByResourceCategory(String resourceCategory);

	Set<Permission> getPrincipalPermissions(Collection<Principal> principals, PermissionType... permissionTypes);

	Set<Role> getDelegatedRoles(Realm parent, Realm child);

}
