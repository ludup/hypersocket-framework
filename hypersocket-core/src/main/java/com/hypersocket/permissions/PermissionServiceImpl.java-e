/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18N;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.PasswordPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.ProfilePermission;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.realm.events.GroupEvent;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.role.events.RoleCreatedEvent;
import com.hypersocket.role.events.RoleDeletedEvent;
import com.hypersocket.role.events.RoleEvent;
import com.hypersocket.role.events.RoleUpdatedEvent;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionService;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Service
public class PermissionServiceImpl extends AuthenticatedServiceImpl
		implements PermissionService, ApplicationListener<SystemEvent> {

	static Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);


	@Autowired
	PermissionRepository repository;

	@Autowired
	RealmService realmService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	@Autowired
	EventService eventService;

	Set<Long> registerPermissionIds = new HashSet<Long>();
	Set<Long> nonSystemPermissionIds = new HashSet<Long>();
	Map<String, PermissionType> registeredPermissions = new HashMap<String, PermissionType>();

	CacheManager cacheManager;
	Cache permissionsCache;
	Cache roleCache;

	@Autowired
	TransactionService transactionService; 

	@PostConstruct
	private void postConstruct() {

		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				PermissionCategory cat = registerPermissionCategory(
						RESOURCE_BUNDLE, "category.permissions");
				registerPermission(SystemPermission.SYSTEM_ADMINISTRATION, cat);
				registerPermission(SystemPermission.SYSTEM, cat);
				registerPermission(SystemPermission.SWITCH_REALM, cat);
			}
		});

		cacheManager = CacheManager.newInstance();
		permissionsCache = new Cache("permissionsCache", 5000, false, false,
				60 * 60, 60 * 60);
		cacheManager.addCache(permissionsCache);

		roleCache = new Cache("roleCache", 5000, false, false, 60 * 60, 60 * 60);
		cacheManager.addCache(roleCache);

		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return repository.getRoleByName(ROLE_ADMINISTRATOR, realm) != null;
			}

			@Override
			public void onCreateRealm(Realm realm) {

				if (log.isInfoEnabled()) {
					log.info("Creating Administrator role for realm "
							+ realm.getName());
				}

				repository.createRole(ROLE_ADMINISTRATOR, realm, false, false,
						true, true);

				if (log.isInfoEnabled()) {
					log.info("Creating Everyone role for realm "
							+ realm.getName());
				}

				Set<Permission> perms = new HashSet<Permission>();
				perms.add(getPermission(AuthenticationPermission.LOGON
						.getResourceKey()));
				perms.add(getPermission(ProfilePermission.READ.getResourceKey()));
				perms.add(getPermission(ProfilePermission.UPDATE
						.getResourceKey()));
				perms.add(getPermission(PasswordPermission.CHANGE.getResourceKey()));

				repository.createRole(ROLE_EVERYONE, realm, false, true, false,
						true, perms);

			}

		});
		eventService.registerEvent(RoleEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleDeletedEvent.class, RESOURCE_BUNDLE);
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
	public Permission registerPermission(PermissionType type,
			PermissionCategory category) {
		registeredPermissions.put(type.getResourceKey(), type);
		return registerPermission(type.getResourceKey(), type.isSystem(),
				category, type.isHidden());
	}

	protected Permission registerPermission(String resourceKey, boolean system,
			PermissionCategory category, boolean hidden) {
		Permission result = repository.getPermissionByResourceKey(resourceKey);
		if (result == null) {
			repository.createPermission(resourceKey, system, category, hidden);
			result = repository.getPermissionByResourceKey(resourceKey);
		}
		registerPermissionIds.add(result.getId());
		if (!system) {
			nonSystemPermissionIds.add(result.getId());
		}
		return result;
	}
	
	@Override
	public Role createRole(String name, Realm realm)
			throws AccessDeniedException, ResourceCreationException {
		assertPermission(RolePermission.CREATE);
		try {
			getRole(name, realm);
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE, "error.role.alreadyExists", name);
			throw ex;
		} catch (ResourceNotFoundException re) {
			return repository.createRole(name, realm, false, false, false,
					false);
		}
	}

	@Override
	public Role createRole(String name, Realm realm,
			List<Principal> principals, List<Permission> permissions)
			throws AccessDeniedException, ResourceCreationException {

		assertPermission(RolePermission.CREATE);
		try {
			getRole(name, realm);
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE, "error.role.alreadyExists", name);
			throw ex;
		} catch (ResourceNotFoundException re) {
			try {
				Role role = new Role();
				role.setName(name);
				role.setRealm(realm);
				repository.saveRole(role, realm,
						principals.toArray(new Principal[0]), permissions);
				for (Principal p : principals) {
					permissionsCache.remove(p);
					roleCache.remove(p);
				}
				eventService.publishEvent(new RoleCreatedEvent(this,
						getCurrentSession(), realm, role));
				return role;
			} catch (Throwable te) {
				eventService.publishEvent(new RoleCreatedEvent(this, name, te,
						getCurrentSession(), realm));
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.resourceCreateError", te.getMessage());
			}
		}
	}

	@Override
	public Permission getPermission(String resourceKey) {
		return repository.getPermissionByResourceKey(resourceKey);
	}

	@Override
	public void assignRole(Role role, Principal principal)
			throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED,
				RolePermission.CREATE, RolePermission.UPDATE);

		try {
			repository.assignRole(role, principal);
			permissionsCache.remove(principal);
			roleCache.remove(principal);
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this, role.getName(), e,
					getCurrentSession(), role.getRealm()));
		}
	}
	
	@Override
	public void assignRole(Role role, Principal... principals)
			throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED,
				RolePermission.CREATE, RolePermission.UPDATE);

		try {
			repository.assignRole(role, principals);
			for(Principal principal : principals) {
				permissionsCache.remove(principal);
				roleCache.remove(principal);
			}
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this, role.getName(), e,
					getCurrentSession(), role.getRealm()));
		}
	}
	
	@Override
	public void unassignRole(Role role, Principal principal)
			throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED,
				RolePermission.UPDATE, RolePermission.UPDATE);

		try {
			repository.unassignRole(role, principal);
			permissionsCache.remove(principal);
			roleCache.remove(principal);
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this, role.getName(), e,
					getCurrentSession(), role.getRealm()));
		}
	}
	
	@Override
	public void unassignRole(Role role, Principal... principals)
			throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED,
				RolePermission.UPDATE, RolePermission.UPDATE);

		try {
			repository.unassignRole(role, principals);
			for(Principal principal : principals) {
				permissionsCache.remove(principal);
				roleCache.remove(principal);
			}
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this, role.getName(), e,
					getCurrentSession(), role.getRealm()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Permission> getPrincipalPermissions(Principal principal) {

		if (!permissionsCache.isElementInMemory(principal)
				|| (permissionsCache.get(principal) == null || permissionsCache
						.isExpired(permissionsCache.get(principal)))) {

			List<Principal> principals = realmService
					.getAssociatedPrincipals(principal);
			Set<Permission> principalPermissions = repository
					.getPrincipalPermissions(principals);

			Set<Role> roles = repository.getAllUserRoles(principal.getRealm());
			for (Role r : roles) {
				principalPermissions.addAll(r.getPermissions());
			}

			roles = repository.getRolesForPrincipal(principals);
			for (Role r : roles) {
				if (r.isAllPermissions()) {
					principalPermissions.addAll(repository.getAllPermissions(
							registerPermissionIds, false));
				}
			}

			permissionsCache.put(new Element(principal, principalPermissions));
		}

		return (Set<Permission>) permissionsCache.get(principal).getObjectValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Role> getPrincipalRoles(Principal principal)
			throws AccessDeniedException {

		if (!roleCache.isElementInMemory(principal)
				|| (roleCache.get(principal) == null 
				|| roleCache.isExpired(roleCache.get(principal)))) {
			
			roleCache.put(new Element(principal, repository
					.getRolesForPrincipal(realmService
							.getAssociatedPrincipals(principal))));
		}

		return (Set<Role>) roleCache.get(principal).getObjectValue();
	}

	private void recurseImpliedPermissions(PermissionType t,
			Set<PermissionType> derivedPermissions) {

		if (t != null && !derivedPermissions.contains(t)) {
			derivedPermissions.add(t);
			if (t.impliesPermissions() != null) {
				for (PermissionType t2 : t.impliesPermissions()) {
					recurseImpliedPermissions(t2, derivedPermissions);
				}
			}
		}
	}

	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, Set<Permission> principalPermissions,
			PermissionType... permissions) throws AccessDeniedException {

		if (principal == null) {
			throw new AccessDeniedException();
		}

		if (!hasSystemPermission(principal)) {

			Set<PermissionType> derivedPrincipalPermissions = new HashSet<PermissionType>();
			for (Permission t : principalPermissions) {
				if (!registeredPermissions.containsKey(t.getResourceKey())) {
					continue;
				}
				switch (strategy) {
				case INCLUDE_IMPLIED:
					recurseImpliedPermissions(
							registeredPermissions.get(t.getResourceKey()),
							derivedPrincipalPermissions);
					break;
				case EXCLUDE_IMPLIED:
					derivedPrincipalPermissions.add(registeredPermissions.get(t
							.getResourceKey()));
					break;
				}

			}

			for (PermissionType t : permissions) {
				for (PermissionType p : derivedPrincipalPermissions) {
					if (t.getResourceKey().equals(p.getResourceKey())) {
						return;
					}
				}
			}

			throw new AccessDeniedException(I18N.getResource(
					getCurrentLocale(), PermissionService.RESOURCE_BUNDLE,
					"error.accessDenied"));

		}
	}

	@Override
	public void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		if (principal == null) {
			if (log.isInfoEnabled()) {
				log.info("Denying permission because principal is null");
			}
			throw new AccessDeniedException();
		}

		if (!hasSystemPermission(principal)) {
			Set<Permission> principalPermissions = getPrincipalPermissions(principal);
			
			if(hasElevatedPermissions()) {
				for(PermissionType perm : getElevatedPermissions()) {
					principalPermissions.add(getPermission(perm.getResourceKey()));
				}
			}
			
			verifyPermission(principal, strategy, principalPermissions,
					permissions);
		}
	}

	@Override
	public boolean hasSystemPermission(Principal principal) {
		return hasSystemPrincipal(getPrincipalPermissions(principal));
	}

	@Override
	public boolean hasPermission(Principal principal, Permission permission) {
		
		for(Permission p : getPrincipalPermissions(principal)) {
			if(p.getResourceKey().equals(permission.getResourceKey())) {
				return true;
			}
		}
		return false;
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
			throws ResourceNotFoundException, AccessDeniedException {

		assertAnyPermission(RolePermission.READ);

		Role role = repository.getRoleByName(name, realm);
		if (role == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE,
					"error.realmNotFound", name);
		}
		return role;
	}

	@Override
	public void deleteRole(Role role) throws AccessDeniedException, ResourceChangeException {
		assertPermission(RolePermission.DELETE);
		try {
			role.getPrincipals().clear();
			role.getPermissions().clear();
			repository.saveRole(role);
			repository.deleteRole(role);
			permissionsCache.removeAll();
			roleCache.removeAll();
			eventService.publishEvent(new RoleDeletedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable te) {
			eventService.publishEvent(new RoleDeletedEvent(this,
					role.getName(), te, getCurrentSession(), role.getRealm()));
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.resourceDeleteError", te.getMessage());
		}
	}

	@Override
	public List<Role> allRoles(Realm realm) throws AccessDeniedException {

		assertAnyPermission(RolePermission.READ);

		return repository.getRolesForRealm(realm);
	}

	@Override
	public List<Permission> allPermissions() {
		return repository.getAllPermissions(registerPermissionIds,
				getCurrentRealm().isSystem());
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
	public void grantPermission(Role role, Permission permission) throws AccessDeniedException, ResourceChangeException {
		
		assertPermission(RolePermission.UPDATE);
		
		try {
			repository.grantPermission(role, permission);
			permissionsCache.removeAll();
			roleCache.removeAll();
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this,
					role.getName(), e, getCurrentSession(), role.getRealm()));
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.resourceUpdateError", e.getMessage());
		}
	}
	
	@Override
	public Role updateRole(Role role, String name, List<Principal> principals,
			List<Permission> permissions) throws AccessDeniedException,
			ResourceChangeException {

		assertPermission(RolePermission.UPDATE);
		try {
			Role anotherRole = getRole(name, role.getRealm());
			if (!anotherRole.getId().equals(role.getId())) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.role.alreadyExists", name);
			}
		} catch (ResourceNotFoundException ne) {
			role.setName(name);
		}
		try {
			Set<Principal> unassignPrincipals = getEntitiesNotIn(principals,
					role.getPrincipals(), new EntityMatch<Principal>() {
						@Override
						public boolean validate(Principal t) {
							return getCurrentRealm().equals(t.getRealm());
						}

					});
			
			Set<Principal> assignPrincipals = new HashSet<Principal>();
			assignPrincipals.addAll(principals);
			assignPrincipals.removeAll(role.getPrincipals());
			Set<Permission> revokePermissions = getEntitiesNotIn(permissions,
					role.getPermissions(), null);
			Set<Permission> grantPermissions = getEntitiesNotIn(
					role.getPermissions(), permissions, null);
			repository.updateRole(role, unassignPrincipals, assignPrincipals,
					revokePermissions, grantPermissions);
			permissionsCache.removeAll();
			roleCache.removeAll();
			eventService.publishEvent(new RoleUpdatedEvent(this,
					getCurrentSession(), role.getRealm(), role));
			return role;
		} catch (Throwable te) {
			eventService.publishEvent(new RoleUpdatedEvent(this,
					role.getName(), te, getCurrentSession(), role.getRealm()));
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.resourceUpdateError", te.getMessage());
		}
	}
	

	@Override
	public Role getRoleById(Long id, Realm realm)
			throws ResourceNotFoundException, AccessDeniedException {

		assertPermission(RolePermission.READ);

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

		return repository.searchRoles(getCurrentRealm(), searchPattern, start,
				length, sorting);
	}

	@Override
	public Role getPersonalRole(Principal principal) {
		return repository.getPersonalRole(principal);
	}

	@Override
	public List<PropertyCategory> getRoleTemplates()
			throws AccessDeniedException {

		assertPermission(RolePermission.READ);

		return new ArrayList<PropertyCategory>();
	}

	@Override
	public void onApplicationEvent(SystemEvent event) {
		
		if(event instanceof GroupEvent || event instanceof UserEvent) {
			permissionsCache.removeAll();
			roleCache.removeAll();
		}
		
	}

	@Override
	public void revokePermissions(final Principal principal, 
			@SuppressWarnings("unchecked") final TransactionAdapter<Principal>... ops) 
					throws ResourceException, AccessDeniedException {
		
		transactionService.doInTransaction(new TransactionCallback<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus status) {
				

				for(TransactionAdapter<Principal> op : ops) {
					op.beforeOperation(principal, new HashMap<String,String>());
				}

				try {
					Role role = repository.getPersonalRole(principal, false);
					if(role!=null) {
						deleteRole(role);
					}
					
					for(Role r : getPrincipalRoles(principal)) {
						repository.unassignRole(r, principal);
					}
				} catch (Throwable e) {
					throw new IllegalStateException(e);
				}
			
				for(TransactionAdapter<Principal> op : ops) {
					op.afterOperation(principal, new HashMap<String,String>());
				}
				
				return principal;
			}
			
		});

	}

	@Override
	public boolean hasRole(Principal principal, Role role) throws AccessDeniedException {
		
		for(Role r : getPrincipalRoles(principal)) {
			if(r.equals(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void assertResourceAccess(AssignableResource resource, Principal principal) throws AccessDeniedException {
		
		boolean found = false;
		Set<Role> principalRoles = getPrincipalRoles(principal);
		for(Role role : resource.getRoles()) {
			if(principalRoles.contains(role)) {
				found = true;
				break;
			}
		}
		
		if(!found) {
			throw new AccessDeniedException();
		}
	}

}
