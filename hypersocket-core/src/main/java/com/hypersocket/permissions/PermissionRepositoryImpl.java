/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.properties.ResourceKeyRestriction;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.util.EmptyIterator;
import com.hypersocket.util.PagedIterator;

@Repository
public class PermissionRepositoryImpl extends AbstractResourceRepositoryImpl<Role>
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
			criteria.setFetchMode("realms", FetchMode.SELECT);
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
		flush();
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
	public Permission updatePermission(String name, boolean system,
			PermissionCategory category, boolean hidden) {

		Permission permission = get("resourceKey", name, Permission.class);
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
	public Role createRole(String name, Realm realm, RoleType type) {
		return createRole(name, realm, false, false, false, false, type);
	}

	@Override
	@Transactional
	public Role createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system, RoleType type) {

		Role role = new Role();
		role.setName(name);
		role.setRealm(realm);
		role.setPermissionRealms(new HashSet<Realm>(Arrays.asList(realm)));
		role.setResourceCategory("role");
		role.setPersonalRole(personalRole);
		role.setAllUsers(allUsers);
		role.setAllPermissions(allPermissions);
		role.setSystem(system);
		role.setType(type);
		
		return (Role) save(role);
	}

	@Override
	@Transactional
	public void updateRole(Role role, Set<Principal> unassignPrincipals,
			Set<Principal> assignPrincipals, Set<Permission> revokePermissions,
			Set<Permission> grantPermissions, Map<String,String> properties,
			 @SuppressWarnings("unchecked") TransactionOperation<Role>... ops) throws ResourceException {
		
		if(StringUtils.isBlank(role.getResourceCategory())) {
			role.setResourceCategory("role");
		}
		saveResource(role, properties, ops);
		unassignRole(role, unassignPrincipals.toArray(new Principal[0]));
		assignRole(role, assignPrincipals.toArray(new Principal[0]));
		revokePermission(role, revokePermissions);
		grantPermissions(role, grantPermissions);
	}

	@Override
	@Transactional
	public void saveRole(Role role, Realm realm, Principal[] principals,
			Collection<Permission> permissions, Map<String,String> properties,
			 @SuppressWarnings("unchecked") TransactionOperation<Role>... ops) throws ResourceException {
		if(StringUtils.isBlank(role.getResourceCategory())) {
			role.setResourceCategory("role");
		}
		saveResource(role, properties, ops);
		assignRole(role, principals);
		grantPermissions(role, permissions);
	}
	
	@Transactional
	@Override
	public void createRole(String name, Realm realm, boolean personalRole,
			boolean allUsers, boolean allPermissions, boolean system,
			Set<Permission> permissions, Map<String,String> properties, RoleType type) throws ResourceException {
		Role role = createRole(name, realm, personalRole, allUsers,
				allPermissions, system, type);
		role.setPermissions(permissions);
		saveResource(role, properties);
		
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
	public void deleteRole(final Role role) {
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
				}, JOIN_ROLES);
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
	public Set<Permission> getDelegatedPrincipalPermissions(Realm currentRealm,
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

		crit = crit.createCriteria("roles");
		
		crit.createCriteria("realms").add(Restrictions.in("id", Arrays.asList(currentRealm.getId())));
		
		crit = crit.createCriteria("principals").add(Restrictions.in("id", ids));

		return new HashSet<Permission>(crit.list());

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

		crit = crit.createCriteria("roles");

		crit.add(Restrictions.isEmpty("realms"));
		
		crit = crit.createCriteria("principals").add(Restrictions.in("id", ids));

		return new HashSet<Permission>(crit.list());

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Principal> getPrincipalsWithPermissions(PermissionType permission) {

		Criteria crit = createCriteria(Principal.class)
				.add(Restrictions.eq("deleted", false))
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));

		Set<Principal> results = new HashSet<Principal>(crit.list());
		return results;
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

		Set<Principal> results = new HashSet<Principal>(crit.list());
		return results;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> getRolesForRealm(Realm realm) {
		return allEntities(Role.class, JOIN_PRINCIPALS_PERMISSIONS,
				new RealmRestriction(realm),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("hidden", false));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> searchRoles(final Realm realm, String searchPattern, String searchColumn,
			int start, int length, ColumnSort[] sorting, boolean includeChildRealms, RoleType... types) {
		return search(Role.class, searchColumn, searchPattern, start, length,
				sorting, new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("hidden", false));
						criteria.setFetchMode("permissions", FetchMode.SELECT);
						criteria.setFetchMode("principals", FetchMode.SELECT);
						criteria.setFetchMode("resources", FetchMode.SELECT);
						criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
						
						if(!includeChildRealms) {
							criteria.add(Restrictions.eq("realm", realm));
						} else {
							criteria.createAlias("realm", "r");
							
							if(realm.isSystem()) {
								criteria.add(Restrictions.or(Restrictions.eq("realm", realm),
										Restrictions.eq("r.publicRealm", Boolean.TRUE)));
							} else {
								criteria.add(Restrictions.or(Restrictions.eq("realm", realm), 
										Restrictions.and(Restrictions.eq("r.parent", realm), 
												Restrictions.eq("r.publicRealm", Boolean.TRUE))));
							}
						}
						
						criteria.add(Restrictions.or(Restrictions.isNull("type"), Restrictions.in("type", types)));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public Long countRoles(final Realm realm, String searchPattern, String searchColumn, boolean includeChildRealms, RoleType... types) {
		return getCount(Role.class, searchColumn, searchPattern,
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
						criteria.add(Restrictions.eq("hidden", false));
						
						if(!includeChildRealms) {
							criteria.add(Restrictions.eq("realm", realm));
						} else {
							criteria.createAlias("realm", "r");
							
							if(realm.isSystem()) {
								criteria.add(Restrictions.or(Restrictions.eq("realm", realm),
										Restrictions.eq("r.publicRealm", Boolean.TRUE)));
							} else {
								criteria.add(Restrictions.or(Restrictions.eq("realm", realm), 
										Restrictions.and(Restrictions.eq("r.parent", realm), 
												Restrictions.eq("r.publicRealm", Boolean.TRUE))));
							}
						}
						
						criteria.add(Restrictions.or(Restrictions.isNull("type"),
								Restrictions.in("type", types)));
						
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


	@Override
	@Transactional
	public Role createPersonalRole(Principal principal) {
		Role r = createRole(
				principal.getRealm().getName() + "/"
						+ principal.getPrincipalName(), principal.getRealm(),
				true, false, false, false, principal.getType()==PrincipalType.USER ? RoleType.USER : RoleType.GROUP);
		r.setPrincipalName(principal.getPrincipalName());
		assignRole(r, principal);
		return r;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Role getPersonalRole(Principal principal) {

		Criteria crit = createCriteria(Role.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.add(Restrictions.eq("personalRole", true));
		crit = crit.createCriteria("principals").add(Restrictions.in("id", Arrays.asList(principal.getId())));

		Set<Role> roles = new HashSet<Role>(crit.list());

		if (roles.isEmpty()) {
			return null;
		} else {
			return roles.iterator().next();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Role> getPersonalRoles(Realm realm) {

		Criteria crit = createCriteria(Role.class)
				.setResultTransformer(
						CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.add(Restrictions.eq("personalRole", true));
		
		crit.createCriteria("realms").add(Restrictions.in("id", Arrays.asList(realm.getId())));
		

		return new HashSet<Role>(crit.list());
	}
	
	@Override
	public long getAssignableResourceCount(final Principal principal) {
		
		return getCount(AssignableResource.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("roles");
				criteria = criteria.createCriteria("principals");
				criteria.add(Restrictions.in("id", Arrays.asList(principal.getId())));
			}
			
		});
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
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Role> getPrincipalRolesForRealm(List<Principal> principals, Realm realm) {

		if (principals == null) {
			return new HashSet<Role>();
		}

		Criteria crit = createCriteria(Role.class).setResultTransformer(
				CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		List<Long> ids = new ArrayList<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}

		crit.createCriteria("realms").add(Restrictions.in("id", Arrays.asList(realm.getId())));
		
		crit = crit.createCriteria("principals")
				.add(Restrictions.in("id", ids));

		Set<Role> roles = new HashSet<Role>(crit.list());
		
		if(principals.get(0).getRealm().equals(realm)) {
			roles.addAll(getAllUserRoles(principals.get(0).getRealm()));
		}
		return roles;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Iterator<Principal> iteratePrincpalsByRole(Realm realm, Collection<Role> roles) {

		if(roles.isEmpty()) {
			return new EmptyIterator<>();
		}
		
		return new PagedIterator<Principal>() {

			@Override
			protected List<Principal> listItems(int start, int pageSize, ColumnSort[] sorting) {
				try {
					return doInTransaction(new TransactionCallback<List<Principal>>() {

						@Override
						public List<Principal> doInTransaction(TransactionStatus status) {
							Criteria crit = createCriteria(Principal.class);
							crit.add(Restrictions.eq("realm", realm));
							crit.add(Restrictions.eq("deleted", false));
							crit.setFirstResult(start);
							crit.setMaxResults(pageSize);
							
							boolean allUsers = false;
							for(Role r : roles) {
								if(r.isAllUsers()) {
									allUsers = true;
									break;
								}
							}
							
							if(!allUsers) {
								crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
									.createCriteria("roles")
									.add(Restrictions.in("id", ResourceUtils.createResourceIdArray(roles)));
							}
							
							return crit.list();
						}
					});
				} catch (ResourceException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				
				
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Set<Principal> getPrincpalsByRole(Realm realm, int max, Collection<Role> roles) {

		/* NOTE: BPS - 17/08/2022 
		 * 
		 * This method produces a very complex query that can choke
		 * MySQL/MariaDB. If you see hangs when this query runs, it 
		 * may be the "optimizer_search_depth" setting. Try setting this
		 * to 0 in the MySQL server configuration.
		 */
		
		if(roles.isEmpty()) {
			return new HashSet<Principal>();
		}
		
		Criteria crit = createCriteria(Principal.class);
		crit.add(Restrictions.eq("realm", realm));
		crit.add(Restrictions.eq("deleted", false));
		crit.setMaxResults(max);
		
		boolean allUsers = false;
		for(Role r : roles) {
			if(r.isAllUsers()) {
				allUsers = true;
				break;
			}
		}
		
		if(!allUsers) {
			crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.add(Restrictions.in("id", ResourceUtils.createResourceIdArray(roles)));
		}
		
		Set<Principal> results = new HashSet<Principal>(crit.list());
		return results;
	}
	
	

	@Override
	@Transactional(readOnly = true)
	public Set<Role> getAllUserRoles(final Realm realm) {

		return new HashSet<Role>(allEntities(Role.class,
				JOIN_PRINCIPALS_PERMISSIONS,
				new RealmRestriction(realm),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("allUsers", true));
						/**
						 * LDP - This is causing problems and I do not know why some systems appear
						 * to lose the realm listing with Everyone not being returned by the query.
						 */
//						criteria.createCriteria("realms").add(Restrictions.in("id", Arrays.asList(realm.getId())));
					}
				}));
	}
	
	@Override
	protected Class<Role> getResourceClass() {
		return Role.class;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Permission> getPermissionsByResourceKeys(final String...resourceKeys) {
		return allEntities(Permission.class,
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.in("resourceKey", resourceKeys));

					}
				}
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> searchNoPersonalNoAllUserRoles(final Realm realm, String searchPattern,
								  int start, int length, ColumnSort[] sorting) {
		return search(Role.class, "name", searchPattern, start, length,
				sorting, new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("hidden", false));
						criteria.add(Restrictions.eq("allUsers", false));
						criteria.add(Restrictions.eq("personalRole", false));
						criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
						criteria.add(Restrictions.eq("realm", realm));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> getPermissionsByIds(Long... ids) {
		return getResourcesByIds(ids);
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
	
		Query q2 = createQuery("delete from Role where realm = :r", true);
		q2.setParameter("r", realm);
		q2.executeUpdate();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Role getRoleByResourceCategory(String resourceCategory) {
		return get("resourceCategory", resourceCategory, Role.class, new DeletedCriteria(false));
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Role> getAllPermissionsRoles(Realm realm) {
		return new HashSet<Role>(allEntities(Role.class,
				JOIN_PRINCIPALS_PERMISSIONS,
				new RealmRestriction(realm),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("allPermissions", true));
					}
		}));
	}


}
