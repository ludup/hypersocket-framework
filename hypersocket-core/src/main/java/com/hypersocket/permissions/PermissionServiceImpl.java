/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.auth.AbstractAuthenticatedService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

@Service
public class PermissionServiceImpl extends AbstractAuthenticatedService
		implements PermissionService {

	@Autowired
	PermissionRepository repository;

	@Autowired
	RealmService realmService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	Set<Long> registerPermissionIds = new HashSet<Long>();
	CacheManager cacheManager;
	Cache permissionsCache;

	@PostConstruct
	private void postConstruct() {

		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				PermissionCategory cat = registerPermissionCategory(
						RESOURCE_BUNDLE, "category.permissions");
				registerPermission(
						SystemPermission.SYSTEM_ADMINISTRATION.getResourceKey(),
						SystemPermission.SYSTEM_ADMINISTRATION.isSystem(), 
						cat, false);
				registerPermission(SystemPermission.SYSTEM.getResourceKey(),
						SystemPermission.SYSTEM.isSystem(),
						cat, true);
			}
		});

		cacheManager = CacheManager.newInstance();
		permissionsCache = new Cache("permissionsCache", 5000, false, false,
				60 * 60, 60 * 60);
		cacheManager.addCache(permissionsCache);
	}

	@Override
	public PermissionCategory registerPermissionCategory(String resourceBundle,
			String resourceKey) {
		PermissionCategory result = repository.getCategoryByKey(resourceBundle,
				resourceKey);
		if (result == null) {
			result = repository.createCategory(resourceBundle, resourceKey);
		}
		return result;
	}

	@Override
	public Permission registerPermission(String resourceKey,
			boolean system, PermissionCategory category) {
		return registerPermission(resourceKey, system, category, false);
	}

	@Override
	public Permission registerPermission(String resourceKey,
			boolean system, PermissionCategory category, boolean hidden) {
		Permission result = repository.getPermissionByResourceKey(resourceKey);
		if (result == null) {
			repository.createPermission(resourceKey, system, category, hidden);
			result = repository.getPermissionByResourceKey(resourceKey);
		}
		registerPermissionIds.add(result.getId());
		return result;
	}

	@Override
	public Role createRole(String name, Realm realm)
			throws AccessDeniedException {

		assertPermission(RolePermission.CREATE);

		return repository.createRole(name, realm, false);
	}

	@Override
	public Role createRole(String name, Realm realm,
			List<Principal> principals, List<Permission> permissions)
			throws AccessDeniedException {

		assertPermission(RolePermission.CREATE);

		Role role = new Role();
		role.setName(name);
		if(!getCurrentRealm().isSystem()) {
			role.setRealm(realm);
		}

		repository.saveRole(role);

		repository.assignRole(role, principals.toArray(new Principal[0]));

		repository.grantPermissions(role, permissions);

		for (Principal p : principals) {
			permissionsCache.remove(p);
		}
		return role;
	}

	@Override
	public Permission getPermission(String resourceKey) {
		return repository.getPermissionByResourceKey(resourceKey);
	}

	@Override
	public void assignRole(Role role, Principal principal)
			throws AccessDeniedException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RolePermission.CREATE,
				RolePermission.UPDATE);

		repository.assignRole(role, principal);

		permissionsCache.remove(principal);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Permission> getPrincipalPermissions(Principal principal) {

		if (!permissionsCache.isElementInMemory(principal)
				|| (permissionsCache.get(principal) == null 
				|| permissionsCache.isExpired(permissionsCache.get(principal)))) {
			permissionsCache.put(new Element(principal, repository
					.getPrincipalPermissions(realmService
							.getAssociatedPrincipals(principal))));
		}

		return (Set<Permission>) permissionsCache.get(principal)
				.getObjectValue();
	}

	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, Set<Permission> principalPermissions,
			PermissionType... permissions) throws AccessDeniedException {

		if (principal == null) {
			throw new AccessDeniedException();
		}

		if (!hasSystemPermission(principal)) {

			switch (strategy) {
			case REQUIRE_ALL_PERMISSIONS: {
				for (PermissionType t : permissions) {
					boolean found = false;
					for (Permission p : principalPermissions) {
						if (t.getResourceKey().equals(p.getResourceKey())) {
							found = true;
							break;
						}
					}
					if (!found) {
						throw new AccessDeniedException(I18N.getResource(
								getCurrentLocale(),
								PermissionService.RESOURCE_BUNDLE,
								"error.accessDenied"));
					}
				}
				break;
			}
			case REQUIRE_ANY: {
				for (PermissionType t : permissions) {
					for (Permission p : principalPermissions) {
						if (t.getResourceKey().equals(p.getResourceKey())) {
							return;
						}
					}
				}

				throw new AccessDeniedException(I18N.getResource(
						getCurrentLocale(), PermissionService.RESOURCE_BUNDLE,
						"error.accessDenied"));
			}
			default: {
				throw new IllegalStateException(
						"Missing PermissionStrategy type! Did you add a new strategy without changing PermissionServiceImpl.verifyPermission?");
			}
			}
		}
	}

	@Override
	public void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		if (principal == null) {
			throw new AccessDeniedException();
		}

		if(!hasSystemPermission(principal)) {
			Set<Permission> principalPermissions = getPrincipalPermissions(principal);
			verifyPermission(principal, strategy, principalPermissions, permissions);
		}
	}

	@Override
	public boolean hasSystemPermission(Principal principal) {
		return hasSystemPrincipal(getPrincipalPermissions(principal));
	}

	protected boolean hasSystemPrincipal(Set<Permission> principalPermissions) {
		for (Permission p : principalPermissions) {
			if (p.getResourceKey().equals(
					SystemPermission.SYSTEM.getResourceKey())
					|| p.getResourceKey().equals(
							SystemPermission.SYSTEM_ADMINISTRATION
									.getResourceKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Principal> getUsersWithPermissions(PermissionType permissions) {
		return repository.getPrincipalsWithPermissions(permissions);
	}

	@Override
	public Role getRole(String name, Realm realm)
			throws ResourceNotFoundException {
		Role role = repository.getRoleByName(name, realm);
		if (role == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE,
					"error.realmNotFound", name);
		}
		return role;
	}

	@Override
	public void deleteRole(Role role) throws AccessDeniedException {

		assertPermission(RolePermission.DELETE);

		repository.deleteRole(role);

		permissionsCache.removeAll();
	}

	@Override
	public List<Role> allRoles(Realm realm) {
		return repository.getRolesForRealm(realm);
	}

	@Override
	public List<Permission> allPermissions() {
		return repository.getAllPermissions(registerPermissionIds, getCurrentRealm().isSystem());
	}

	private <T> Set<T> getEntitiesNotIn(Collection<T> source,
			Collection<T> from, EntityMatch<T> validation) {

		Set<T> result = new HashSet<T>();

		for (T t : from) {
			if (!source.contains(t)) {
				if (validation == null || validation.validate(t)) {
					result.add(t);
				}
			}
		}

		return result;
	}

	@Override
	public Role updateRole(Role role, String name, List<Principal> principals,
			List<Permission> permissions) throws AccessDeniedException {

		assertPermission(RolePermission.UPDATE);

		role.setName(name);

		repository.saveRole(role);

		Set<Principal> unassignPrincipals = getEntitiesNotIn(principals,
				role.getPrincipals(), new EntityMatch<Principal>() {
					@Override
					public boolean validate(Principal t) {
						return getCurrentRealm().equals(t.getRealm());
					}

				});
		Set<Principal> assignPrincipals = getEntitiesNotIn(
				role.getPrincipals(), principals, new EntityMatch<Principal>() {
					@Override
					public boolean validate(Principal t) {
						return getCurrentRealm().equals(t.getRealm());
					}

				});
		repository.unassignRole(role,
				unassignPrincipals.toArray(new Principal[0]));

		repository.assignRole(role, assignPrincipals.toArray(new Principal[0]));

		Set<Permission> revokePermissions = getEntitiesNotIn(permissions,
				role.getPermissions(), null);
		Set<Permission> grantPermissions = getEntitiesNotIn(
				role.getPermissions(), permissions, null);

		repository.revokePermission(role, revokePermissions);

		repository.grantPermissions(role, grantPermissions);

		permissionsCache.removeAll();

		return role;
	}

	@Override
	public Role getRoleById(Long id, Realm realm)
			throws ResourceNotFoundException {

		Role role = repository.getRoleById(id);
		if (role.getRealm() != null && !role.getRealm().equals(realm)) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE,
					"error.invalidRole", id);
		}
		return role;
	}

	@Override
	public Permission getPermissionById(Long id) {
		return repository.getPermissionById(id);
	}

	private interface EntityMatch<T> {
		boolean validate(T t);
	}

	@Override
	public Long getRoleCount(String searchPattern) throws AccessDeniedException {
		assertPermission(RolePermission.READ);
		
		return repository.countRoles(getCurrentRealm(), searchPattern);
	}

	@Override
	public List<?> getRoles(String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		return repository.searchRoles(getCurrentRealm(), searchPattern, start, length, sorting);
	}

	@Override
	public Role getPersonalRole(Principal principal) {
		return repository.getPersonalRole(principal);
	}
}
