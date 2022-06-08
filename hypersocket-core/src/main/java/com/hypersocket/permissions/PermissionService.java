/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.tables.ColumnSort;

public interface PermissionService extends AuthenticatedService {

	static final String RESOURCE_BUNDLE = "PermissionService";
	static final String ROLE_REALM_ADMINISTRATOR = "Realm Administrator";
	static final String OLD_ROLE_ADMINISTRATOR = "Administrator";
	static final String ROLE_EVERYONE = "Everyone";
	static final String ROLE_SYSTEM_ADMINISTRATOR = "System Administrator";

	PermissionCategory registerPermissionCategory(String resourceBundle, String resourceKey);

	Permission getPermission(String resourceKey);

	List<Permission> getPermissions(String...resourceKeys);

	Role createRole(String name, Realm realm, RoleType type) throws AccessDeniedException, ResourceException;

	void unassignRole(Role customerRole, Principal principal) throws AccessDeniedException, ResourceException;
	
	void unassignRole(Role customerRole, Principal... principals) throws AccessDeniedException, ResourceException;
	
	void assignRole(Role role, Principal principal) throws AccessDeniedException;

	void assignRole(Role role, Principal... principals) throws AccessDeniedException;

	void verifyPermission(Principal principal, PermissionStrategy strategy, PermissionType... permission)
			throws AccessDeniedException;
	
	void verifyPermission(Realm realm, Principal principal, PermissionStrategy strategy, PermissionType... permission)
			throws AccessDeniedException;

	Role getRole(String name, Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	void deleteRole(Role name) throws ResourceException, AccessDeniedException;

	List<Role> allRoles(Realm realm) throws AccessDeniedException;

	List<Permission> allPermissions();

	Role createRole(String name, Realm realm, List<Principal> principals, List<Permission> permissions, List<Realm> realms,
			Map<String, String> properties, RoleType type, boolean allUsers, boolean allPerms, boolean allRealms)
			throws AccessDeniedException, ResourceException;
	
	Role createRole(String name, Realm realm, List<Principal> principals, List<Permission> permissions, List<Realm> realms,
			Map<String, String> properties, boolean isPrincipalRole, boolean isSystemRole, RoleType type, boolean allUsers, boolean allPerms, boolean allRealms)
			throws AccessDeniedException, ResourceException;

	Role updateRole(Role role, String name, List<Principal> principals, List<Permission> permissions, List<Realm> realms, Map<String,String> properties, boolean allUsers, boolean allPerm, boolean allRealms)
			throws AccessDeniedException, ResourceException;

	Role getRoleById(Long id, Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	Permission getPermissionById(Long perm);

	Set<Permission> getPrincipalPermissions(Principal principal) throws AccessDeniedException;

	boolean hasSystemPermission(Principal principal);

	boolean hasAdministrativePermission(Principal principal);
	
	Long getRoleCount(String searchPattern, String searchColumn,  boolean includeChildRealms, RoleType... types) throws AccessDeniedException;

	List<?> getRoles(String searchPattern, String searchColumn, int start, int length, ColumnSort[] sorting,  boolean includeChildRealms, RoleType... types)
			throws AccessDeniedException;

	List<?> getNoPersonalNoAllUsersRoles(String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException;

	Role getPersonalRole(Principal principal);

	Set<Role> getPrincipalRoles(Principal principal);

	Set<Role> getPrincipalNonPersonalRoles(Principal principal);

	Set<Role> getPrincipalNonPersonalNonAllUserRoles(Principal principal);

	String getRoleProperty(Role resource, String resourceKey);

	boolean getRoleBooleanProperty(Role resource, String resourceKey);

	Long getRoleLongProperty(Role resource, String resourceKey);

	int getRoleIntProperty(Role resource, String resourceKey);
	
	Collection<PropertyCategory> getRoleTemplate() throws AccessDeniedException;
	
	Collection<PropertyCategory> getRoleProperties(Role role) throws AccessDeniedException;

	Permission registerPermission(PermissionType type, PermissionCategory category);

	void grantPermission(Role everyone, Permission permission) throws AccessDeniedException, ResourceException;

	void revokePermissions(Principal principal, @SuppressWarnings("unchecked") TransactionAdapter<Principal>... ops)
			throws ResourceException, AccessDeniedException;

	boolean hasPermission(Principal principal, Permission permission);
	
	boolean hasPermission(Principal principal, PermissionType permission);

	boolean hasAllPermissions(Principal principal, Permission...permissions);

	boolean hasAnyPermission(Principal principal, Permission...permissions);

	boolean hasRole(Principal principal, Role role);

	void assertResourceAccess(AssignableResource resource, Principal principal) throws AccessDeniedException;
	
	Role createRoleAndAssignPrincipals(String roleName, Realm realm,Principal...principals) throws ResourceException,AccessDeniedException;

	Set<String> getRolePropertyNames();

	boolean hasRole(Principal principal, Collection<Role> roles);

	boolean hasEveryoneRole(Collection<Role> roles, Realm realm) throws AccessDeniedException, ResourceNotFoundException;

	Collection<Role> getRolesByPrincipal(Principal principal);

	void registerAssignableRepository(Class<? extends AssignableResource> clz,
			AbstractAssignableResourceRepository<?> repository);

	Iterator<Principal> resolveUsers(Iterator<Principal> principals);

	Iterator<Principal> resolveUsers(Collection<Role> roles, Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	Set<Principal> getPrincipalsByRole(Realm realm, int maximum, Role... roles) throws ResourceNotFoundException, AccessDeniedException;

	Iterator<Principal> iteratePrincipalsByRole(Realm realm, Role... roles) throws ResourceNotFoundException, AccessDeniedException;

	Set<Principal> getPrincipalsByRole(Realm realm, int maximum, Collection<Role> roles) throws ResourceNotFoundException;

	Iterator<Principal> iteratePrincipalsByRole(Realm realm, Collection<Role> roles) throws ResourceNotFoundException;

	void deleteResources(List<Role> resources) throws ResourceException, AccessDeniedException;
	
	List<Role> getResourcesByIds(Long...ids) throws AccessDeniedException;

	Role getRealmAdministratorRole(Realm realm);

	Role getSystemAdministratorRole();

	void revokePermissionsNonTransactional(Principal principal);

	 Role getRoleById(Long id);

	Set<Permission> getPrincipalPermissions(Realm realm, Principal principal);

	Set<Role> getPrincipalRolesForRealm(Principal principal, Realm realm);

	Set<Realm> getPrincipalPermissionRealms(Principal principal);

	Set<Role> getAllUserRoles();

	void assertAdministrativeAccess() throws AccessDeniedException;
	
	Set<Role> getPersonalRoles(Realm realm);

}
