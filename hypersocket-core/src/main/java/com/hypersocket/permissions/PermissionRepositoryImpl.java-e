/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceKeyRestriction;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public class PermissionRepositoryImpl extends AbstractRepositoryImpl<Long>
		implements PermissionRepository {

	CriteriaConfiguration JOIN_PERMISSIONS = new CriteriaConfiguration() {
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("permissions", FetchMode.SELECT);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	CriteriaConfiguration JOIN_PRINCIPALS_PERMISSIONS = new CriteriaConfiguration() {
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("permissions", FetchMode.SELECT);
			criteria.setFetchMode("principals", FetchMode.SELECT);
			criteria.setFetchMode("resources", FetchMode.SELECT);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	CriteriaConfiguration JOIN_ROLES = new CriteriaConfiguration() {
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("roles", FetchMode.SELECT);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	@Override
	@Transactional
	public PermissionCategory createCategory(String resourceBundle,
			String resourceKey) {

		PermissionCategory category = new PermissionCategory();
		category.setResourceBundle(resourceBundle);
		category.setResourceKey(resourceKey);
		save(category);
		return category;
	}

	@Override
	@Transactional
	public Permission createPermission(String name, boolean system,
			PermissionCategory category, boolean hidden) {

		Permission permission = new Permission();
		permission.setResourceKey(name);
		permission.setCategory(category);
		permission.setHidden(hidden);
		permission.setSystem(system);
		save(permission);
		flush();
		refresh(permission);
		return permission;
	}

	@Override
	@Transactional
	public Role createRole(String name, Realm realm) {
		return createRole(name, realm, false, false, false, false);
	}

	@Override
	@Transactional
	public Role createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system) {

		Role role = new Role();
		role.setName(name);
		role.setRealm(realm);
		role.setResourceCategory("role");
		role.setPersonalRole(personalRole);
		role.setAllUsers(allUsers);
		role.setAllPermissions(allPermissions);
		role.setSystem(system);
		
		save(role);
		return role;
	}

	@Override
	@Transactional
	public void updateRole(Role role, Set<Principal> unassignPrincipals,
			Set<Principal> assignPrincipals, Set<Permission> revokePermissions,
			Set<Permission> grantPermissions) {
		
		if(StringUtils.isBlank(role.getResourceCategory())) {
			role.setResourceCategory("role");
		}
		save(role);
		unassignRole(role, unassignPrincipals.toArray(new Principal[0]));
		assignRole(role, assignPrincipals.toArray(new Principal[0]));
		revokePermission(role, revokePermissions);
		grantPermissions(role, grantPermissions);
	}

	@Override
	@Transactional
	public void saveRole(Role role, Realm realm, Principal[] principals,
			Collection<Permission> permissions) {
		if(StringUtils.isBlank(role.getResourceCategory())) {
			role.setResourceCategory("role");
		}
		save(role);
		assignRole(role, principals);
		grantPermissions(role, permissions);
	}

	@Transactional
	@Override
	public void createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system,
			Set<Permission> permissions) {
		Role role = createRole(name, realm, personalRole, allUsers,
				allPermissions, system);
		role.setPermissions(permissions);
		save(role);
	}
	@Override
	@Transactional(readOnly = true)
	public PermissionCategory getCategoryByKey(String resourceBundle,
			String resourceKey) {
		return get("resourceBundle", resourceBundle, PermissionCategory.class,
				new ResourceKeyRestriction(resourceKey),
				new DistinctRootEntity());
	}

	@Override
	@Transactional(readOnly = true)
	public PermissionCategory getCategoryById(Long id) {
		return getCategory("id", id);
	}

	protected PermissionCategory getCategory(String column, Object value) {
		return get(column, value, PermissionCategory.class, JOIN_PERMISSIONS);
	}

	@Override
	@Transactional(readOnly = true)
	public Permission getPermissionByResourceKey(String name) {
		return getPermission("resourceKey", name);
	}

	@Override
	@Transactional(readOnly = true)
	public Permission getPermissionById(Long id) {
		return getPermission("id", id);
	}

	protected Permission getPermission(String column, Object value) {
		return get(column, value, Permission.class, JOIN_ROLES);
	}

	@Override
	@Transactional(readOnly = true)
	public Role getRoleByName(String name, Realm realm) {
		return get("name", name, Role.class, JOIN_PRINCIPALS_PERMISSIONS,
				new RealmRestriction(realm));
	}

	@Override
	@Transactional(readOnly = true)
	public Role getRoleById(Long id) {
		return get("id", id, Role.class, JOIN_PRINCIPALS_PERMISSIONS);
	}

	@Override
	@Transactional
	public void grantPermission(Role role, Permission permission) {
		role.getPermissions().add(permission);
		save(role);
	}

	@Override
	@Transactional
	public void revokePermission(Role role, Permission permission) {
		role.getPermissions().remove(permission);
		save(role);
	}

	@Override
	@Transactional
	public void grantPermissions(Role role, Collection<Permission> permissions) {
		role.getPermissions().addAll(permissions);
		save(role);
	}

	@Override
	@Transactional
	public void revokePermission(Role role, Collection<Permission> permissions) {
		role.getPermissions().removeAll(permissions);
		save(role);
	}

	@Override
	@Transactional
	public void deleteCategory(PermissionCategory category) {
		delete(category);
	}

	@Override
	@Transactional
	public void deletePermission(Permission permission) {
		delete(permission);
	}

	@Override
	@Transactional
	public void deleteRole(Role role) {
		delete(role);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PermissionCategory> getAllCatgories() {
		return allEntities(PermissionCategory.class, JOIN_PERMISSIONS);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Permission> getAllPermissions(final Set<Long> permissions,
			final boolean includeSystem) {
		return allEntities(Permission.class,
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						if (!includeSystem) {
							criteria.add(Restrictions.and(
									Restrictions.eq("system", false),
									Restrictions.in("id", permissions)));
						} else {
							criteria.add(Restrictions.in("id", permissions));
						}
					}
				}, JOIN_ROLES, new HiddenCriteria(false));
	}

	@Override
	@Transactional
	public void saveRole(Role role) {
		if(StringUtils.isBlank(role.getResourceCategory())) {
			role.setResourceCategory("role");
		}
		save(role);
	}

	@Override
	@Transactional
	public void assignRole(Role role, Principal... principals) {

		for (Principal p : principals) {
			role.getPrincipals().add(p);
		}

		save(role);

	}

	@Override
	@Transactional
	public void unassignRole(Role role, Principal... principals) {
		for (Principal p : principals) {
			role.getPrincipals().remove(p);
		}

		save(role);

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Permission> getPrincipalPermissions(
			Collection<Principal> principals, PermissionType... permissionTypes) {

		if (principals == null) {
			return new HashSet<Permission>();
		}

		Criteria crit = createCriteria(Permission.class).setResultTransformer(
				CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		List<String> list = new ArrayList<String>();
		for (PermissionType t : permissionTypes) {
			list.add(t.getResourceKey());
		}

		if (list.size() > 0) {
			crit = crit.add(Restrictions.in("resourceKey", list));
		}

		List<Long> ids = new ArrayList<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}

		crit = crit.createCriteria("roles").createCriteria("principals")
				.add(Restrictions.in("id", ids));

		return new HashSet<Permission>(crit.list());

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Principal> getPrincipalsWithPermissions(PermissionType permission) {

		Criteria crit = createCriteria(Principal.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));

		return new HashSet<Principal>(crit.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Role> getRolesWithPermissions(PermissionType permission) {

		Criteria crit = createCriteria(Role.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));

		return new HashSet<Role>(crit.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Role> getRolesWithPermissions(Permission permission) {

		Criteria crit = createCriteria(Role.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));

		return new HashSet<Role>(crit.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Principal> getPrincipalsWithPermissions(Permission permission) {

		Criteria crit = createCriteria(Principal.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));

		return new HashSet<Principal>(crit.list());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> getRolesForRealm(Realm realm) {
		return allEntities(Role.class, JOIN_PRINCIPALS_PERMISSIONS,
				new RealmRestriction(realm),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("personalRole", false));
						criteria.add(Restrictions.eq("hidden", false));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> searchRoles(final Realm realm, String searchPattern,
			int start, int length, ColumnSort[] sorting) {
		return search(Role.class, "name", searchPattern, start, length,
				sorting, new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("personalRole", false));
						criteria.add(Restrictions.eq("hidden", false));
						criteria.setFetchMode("permissions", FetchMode.SELECT);
						criteria.setFetchMode("principals", FetchMode.SELECT);
						criteria.setFetchMode("resources", FetchMode.SELECT);
						criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
						criteria.add(Restrictions.eq("realm", realm));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public Long countRoles(final Realm realm, String searchPattern) {
		return getCount(Role.class, "name", searchPattern,
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
						criteria.add(Restrictions.eq("personalRole", false));
						criteria.add(Restrictions.eq("hidden", false));
						criteria.add(Restrictions.or(
								Restrictions.eq("realm", realm),
								Restrictions.isNull("realm")));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Permission> getPermissionsByCategory(
			final PermissionCategory category) {
		return allEntities(Permission.class, JOIN_ROLES,
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("category", category));

					}
				});
	}

	private Role createPersonalRole(Principal principal) {
		Role r = createRole(
				principal.getRealm().getName() + "/"
						+ principal.getPrincipalName(), principal.getRealm(),
				true, false, false, false);
		assignRole(r, principal);
		return r;
	}

	@Override
	@Transactional
	public Role getPersonalRole(Principal principal) {
		return getPersonalRole(principal, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Role getPersonalRole(Principal principal, boolean createIfNotFound) {

		Criteria crit = createCriteria(Role.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.add(Restrictions.eq("personalRole", true))
				.createCriteria("principals")
				.add(Restrictions.in("id", Arrays.asList(principal.getId())));

		Set<Role> roles = new HashSet<Role>(crit.list());

		if (roles.isEmpty() && createIfNotFound) {
			return createPersonalRole(principal);
		} else if(roles.isEmpty()) {
			return null;
		} else {
			return roles.iterator().next();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Role> getRolesForPrincipal(List<Principal> principals) {

		if (principals == null) {
			return new HashSet<Role>();
		}

		Criteria crit = createCriteria(Role.class).setResultTransformer(
				CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		List<Long> ids = new ArrayList<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}

		crit = crit.createCriteria("principals")
				.add(Restrictions.in("id", ids));

		Set<Role> roles = new HashSet<Role>(crit.list());
		roles.addAll(getAllUserRoles(principals.get(0).getRealm()));
		return roles;
	}

	@Override
	@Transactional(readOnly = true)
	public Set<Role> getAllUserRoles(Realm realm) {

		return new HashSet<Role>(allEntities(Role.class,
				JOIN_PRINCIPALS_PERMISSIONS, new RealmRestriction(realm),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("allUsers", true));
					}
				}));
	}


}
