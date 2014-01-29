/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepository;

public interface PermissionRepository extends AbstractRepository<Long> {

	public PermissionCategory createCategory(String resourceBundle,
			String resourceKey);

	public Permission createPermission(String resourceKey,
			PermissionCategory category, boolean hidden);

	public Role createRole(String name, Realm realm);

	public PermissionCategory getCategoryById(Long id);

	public PermissionCategory getCategoryByKey(String resourceBundle, String resourceKey);

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

	public List<Permission> getAllPermissions();

	public void assignRole(Role role, Principal... principal);

	public void unassignRole(Role role, Principal... principal);

	Set<Permission> getPrincipalPermissions(Collection<Principal> principals,
			PermissionType... permissionTypes);
	
	Set<Principal> getPrincipalsWithPermissions(PermissionType permissions);

	public List<Role> getRolesForRealm(Realm realm);

	public List<Permission> getPermissionsByCategory(PermissionCategory cat1);

	void saveRole(Role role);

	Set<Principal> getPrincipalsWithPermissions(Permission permission);

	Set<Role> getRolesWithPermissions(PermissionType permission);

	Set<Role> getRolesWithPermissions(Permission permission);

}
