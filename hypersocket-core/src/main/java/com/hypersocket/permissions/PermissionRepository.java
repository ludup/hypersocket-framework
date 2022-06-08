/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tables.ColumnSort;

public interface PermissionRepository extends AbstractResourceRepository<Role> {

	PermissionCategory createCategory(String resourceBundle,
			String resourceKey);

	Permission createPermission(String resourceKey, boolean system,
			PermissionCategory category, boolean hidden);

	Role createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system, RoleType type);

	PermissionCategory getCategoryById(Long id);

	PermissionCategory getCategoryByKey(String resourceBundle,
			String resourceKey);

	Permission getPermissionById(Long id);

	Permission getPermissionByResourceKey(String name);

	Role getRoleById(Long id);

	Role getRoleByName(String name, Realm realm);

	void grantPermission(Role role, Permission permissions);

	void revokePermission(Role role, Permission permissions);

	void grantPermissions(Role role, Collection<Permission> permissions);

	void revokePermission(Role role, Collection<Permission> permissions);

	void deleteCategory(PermissionCategory category);

	void deletePermission(Permission permission);

	void deleteRole(Role role);

	List<PermissionCategory> getAllCatgories();

	List<Permission> getAllPermissions(Set<Long> registered,
			boolean includeSystem);

	void assignRole(Role role, Principal... principal);

	void unassignRole(Role role, Principal... principal);

	Set<Permission> getDelegatedPrincipalPermissions(Realm currentRealm,
			Collection<Principal> principals,
			PermissionType... permissionTypes);

	Set<Principal> getPrincipalsWithPermissions(PermissionType permissions);

	List<Role> getRolesForRealm(Realm realm);

	List<Permission> getPermissionsByCategory(PermissionCategory cat1);

	void saveRole(Role role);

	Set<Principal> getPrincipalsWithPermissions(Permission permission);

	Set<Role> getRolesWithPermissions(PermissionType permission);

	Set<Role> getRolesWithPermissions(Permission permission);

	List<Role> searchRoles(Realm realm, String searchPattern, String searchColumn, int start,
			int length, ColumnSort[] sorting, boolean includeChildRealms, RoleType... types);

	Long countRoles(Realm realm, String searchPattern, String searchColumn, boolean includeChildRealms, RoleType... types);

	Role getPersonalRole(Principal principal);

	Role createRole(String name, Realm realm, RoleType type);

	Set<Role> getRolesForPrincipal(List<Principal> associatedPrincipals);

	Iterator<Principal> iteratePrincpalsByRole(Realm realm, Collection<Role> associatedPrincipals);

	Set<Principal> getPrincpalsByRole(Realm realm, int max, Collection<Role> associatedPrincipals);

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
	
	Set<Role> getPersonalRoles(Realm realm);

	long getAssignableResourceCount(Principal principal);

	List<Permission> getPermissionsByResourceKeys(final String...resourceKeys);

	List<Role> searchNoPersonalNoAllUserRoles(final Realm realm, String searchPattern,
													 int start, int length, ColumnSort[] sorting);
	List<Role> getPermissionsByIds(Long...ids);

	Permission updatePermission(String name, boolean system, PermissionCategory category, boolean hidden);

	void deleteRealm(Realm realm);

	Role getRoleByResourceCategory(String resourceCategory);

	Set<Permission> getPrincipalPermissions(Collection<Principal> principals, PermissionType... permissionTypes);

	Set<Role> getPrincipalRolesForRealm(List<Principal> principals, Realm realm);

	Collection<Role> getAllPermissionsRoles(Realm currentRealm);

}
