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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.attributes.role.RoleAttributeRepository;
import com.hypersocket.attributes.role.RoleAttributeService;
import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.auth.InvalidAuthenticationContext;
import com.hypersocket.cache.CacheService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18N;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.PasswordPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.ProfilePermission;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.realm.events.GroupEvent;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
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
import com.hypersocket.util.ProxiedIterator;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class PermissionServiceImpl extends AuthenticatedServiceImpl
		implements PermissionService, ApplicationListener<SystemEvent> {

	private static final boolean CACHE_PERMISSIONS = "true".equals(System.getProperty("hypersocket.cachePermissions", "true"));

	static Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

	@Autowired
	private PermissionRepository repository;

	@Autowired
	private RealmService realmService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	@Autowired
	private EventService eventService;

	private Set<Long> registerPermissionIds = new HashSet<Long>();
	private Set<Long> nonSystemPermissionIds = new HashSet<Long>();
	private Map<String, PermissionType> registeredPermissions = new HashMap<String, PermissionType>();

	@SuppressWarnings("rawtypes")
	private Cache<Object, Set> permissionsCache;

	@SuppressWarnings("rawtypes")
	private Cache<Object, Set> roleCache;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private RoleAttributeService attributeService;

	@Autowired
	private RoleAttributeRepository attributeRepository;

	private Map<Class<? extends AssignableResource>, AbstractAssignableResourceRepository<?>> repositories = new HashMap<Class<? extends AssignableResource>, AbstractAssignableResourceRepository<?>>();

	@PostConstruct
	private void postConstruct() {

		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				PermissionCategory cat = registerPermissionCategory(RESOURCE_BUNDLE, "category.permissions");
				registerPermission(SystemPermission.SYSTEM_ADMINISTRATION, cat);
				registerPermission(SystemPermission.SYSTEM, cat);
				registerPermission(SystemPermission.SWITCH_REALM, cat);
			}
		});

		permissionsCache = cacheService.getCacheOrCreate("permissionsCache", Object.class, Set.class);

		roleCache = cacheService.getCacheOrCreate("roleCache", Object.class, Set.class);

		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return repository.getRoleByName(ROLE_REALM_ADMINISTRATOR, realm) != null;
			}

			@Override
			public void onCreateRealm(Realm realm) throws ResourceException {

				if (log.isInfoEnabled()) {
					log.info("Creating Administrator role for realm " + realm.getName());
				}

				repository.createRole(ROLE_REALM_ADMINISTRATOR, realm, false, false, true, true, RoleType.BUILTIN);

				if (log.isInfoEnabled()) {
					log.info("Creating Everyone role for realm " + realm.getName());
				}

				Set<Permission> perms = new HashSet<Permission>();
				perms.add(getPermission(AuthenticationPermission.LOGON.getResourceKey()));
				perms.add(getPermission(ProfilePermission.READ.getResourceKey()));
				perms.add(getPermission(ProfilePermission.UPDATE.getResourceKey()));
				perms.add(getPermission(PasswordPermission.CHANGE.getResourceKey()));

				repository.createRole(ROLE_EVERYONE, realm, false, true, false, true, perms,
						new HashMap<String, String>(), RoleType.BUILTIN);

			}
			
			@Override
			public Integer getWeight() {
				return Integer.MIN_VALUE;
			}

		});
		eventService.registerEvent(RoleEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(RoleDeletedEvent.class, RESOURCE_BUNDLE);

		EntityResourcePropertyStore.registerResourceService(Role.class, repository);

		repository.loadPropertyTemplates("roleTemplate.xml");
	}

	@Override
	public void registerAssignableRepository(Class<? extends AssignableResource> clz,
			AbstractAssignableResourceRepository<?> repository) {
		repositories.put(clz, repository);
	}

	@Override
	public PermissionCategory registerPermissionCategory(String resourceBundle, String resourceKey) {
		PermissionCategory result = repository.getCategoryByKey(resourceBundle, resourceKey);
		if (result == null) {
			result = repository.createCategory(resourceBundle, resourceKey);
		}
		return result;
	}

	@Override
	protected Set<Role> getCurrentRoles() {
		return getPrincipalRoles(getCurrentPrincipal());
	}
	
	@Override
	public Permission registerPermission(PermissionType type, PermissionCategory category) {
		registeredPermissions.put(type.getResourceKey(), type);
		return registerPermission(type.getResourceKey(), type.isSystem(), category, type.isHidden());
	}

	protected Permission registerPermission(String resourceKey, boolean system, PermissionCategory category,
			boolean hidden) {
		Permission result = repository.getPermissionByResourceKey(resourceKey);
		if (result == null) {
			repository.createPermission(resourceKey, system, category, hidden);
			result = repository.getPermissionByResourceKey(resourceKey);
		} else {
			result = repository.updatePermission(resourceKey, system, category, hidden);
		}
		registerPermissionIds.add(result.getId());
		if (!system) {
			nonSystemPermissionIds.add(result.getId());
		}
		return result;
	}

	@Override
	public Role createRole(String name, Realm realm, RoleType type) throws AccessDeniedException, ResourceException {
		assertPermission(RolePermission.CREATE);
		try {
			getRole(name, realm);
			ResourceCreationException ex = new ResourceCreationException(RESOURCE_BUNDLE, "error.role.alreadyExists",
					name);
			throw ex;
		} catch (ResourceNotFoundException re) {
			return createRole(name, realm, Collections.<Principal>emptyList(), Collections.<Permission>emptyList(),
					null, null, false, false, type, false, false, false);
		}
	}

	@Override
	public Role createRole(String name, Realm realm, List<Principal> principals, List<Permission> permissions, List<Realm> realms,
			Map<String, String> properties, RoleType type, boolean allUsers, boolean allPerms, boolean allRealms) throws AccessDeniedException, ResourceException {
		return createRole(name, realm, principals, permissions, realms, properties, false, false, type, allUsers, allPerms, allRealms);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Role createRole(String name, Realm realm, List<Principal> principals, List<Permission> permissions, List<Realm> realms,
			Map<String, String> properties, boolean isPrincipalRole, boolean isSystemRole, RoleType type, boolean allUsers, boolean allPerms, boolean allRealms)
			throws AccessDeniedException, ResourceException {

		assertPermission(RolePermission.CREATE);
		try {
			getRole(name, realm);
			ResourceCreationException ex = new ResourceCreationException(RESOURCE_BUNDLE, "error.role.alreadyExists",
					name);
			throw ex;
		} catch (ResourceNotFoundException re) {
			try {
				Role role = new Role();
				role.setName(name);
				role.setRealm(realm);
				role.getPermissionRealms().addAll(realms);
				
				role.setAllPermissions(allPerms);
				role.setAllUsers(allUsers);
				role.setAllRealms(allRealms);
				role.setPersonalRole(isPrincipalRole);
				role.setSystem(isSystemRole);
				role.setType(type);
				repository.saveRole(role, realm, principals.toArray(new Principal[0]), permissions, properties,
						new TransactionAdapter<Role>() {

							@Override
							public void afterOperation(Role resource, Map<String, String> properties) {
								saveRoleAttributes(resource, properties);
							}

						});
				synchronized (permissionsCache) {
					permissionsCache.removeAll();
					roleCache.removeAll();
				}
				eventService.publishEvent(new RoleCreatedEvent(this, getCurrentSession(), realm, role, principals));
				return role;
			} catch (Throwable te) {
				eventService.publishEvent(new RoleCreatedEvent(this, name, te, getCurrentSession(), realm));
				throw new ResourceCreationException(te, RESOURCE_BUNDLE, "error.resourceCreateError", te.getMessage());
			}
		}
	}

	@Override
	public Permission getPermission(String resourceKey) {
		return repository.getPermissionByResourceKey(resourceKey);
	}

	@Override
	public List<Permission> getPermissions(String...resourceKeys) {
		return repository.getPermissionsByResourceKeys(resourceKeys);
	}

	@Override
	public void assignRole(Role role, Principal principal) throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, RolePermission.CREATE, RolePermission.UPDATE);

		try {
			if(role.isPersonalRole()) {
				throw new AccessDeniedException("You cannot assign a personal role to any principal");
			}
			repository.assignRole(role, principal);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();	
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					Arrays.asList(principal), new ArrayList<Principal>()));
		} catch (Throwable e) {
			eventService
					.publishEvent(new RoleUpdatedEvent(this, role.getName(), e, getCurrentSession(), role.getRealm()));
			throw e;
		}
	}

	@Override
	public void assignRole(Role role, Principal... principals) throws AccessDeniedException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, RolePermission.CREATE, RolePermission.UPDATE);

		try {
			if(role.isPersonalRole()) {
				throw new AccessDeniedException("You cannot assign a personal role to any principal");
			}
			repository.assignRole(role, principals);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					Arrays.asList(principals), new ArrayList<Principal>()));
		} catch (Throwable e) {
			eventService
					.publishEvent(new RoleUpdatedEvent(this, role.getName(), e, getCurrentSession(), role.getRealm()));
			throw e;
		}
	}

	@Override
	public void unassignRole(Role role, Principal principal) throws AccessDeniedException, ResourceException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, RolePermission.UPDATE, RolePermission.UPDATE);


		try {
			checkSystemAdministratorAssignments(role);
			if(role.isPersonalRole()) {
				throw new AccessDeniedException("You cannot unassign a personal role from any principal");
			}
			repository.unassignRole(role, principal);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					new ArrayList<Principal>(), Arrays.asList(principal)));
		} catch (Throwable e) {
			eventService.publishEvent(new RoleUpdatedEvent(this, role.getName(), e, getCurrentSession(), role.getRealm()));
			throw e;
		}
	
	}

	private void checkSystemAdministratorAssignments(Role role) throws ResourceException, AccessDeniedException {
		if(role.getName().equals(ROLE_SYSTEM_ADMINISTRATOR)) {
			Iterator<Principal> admins = iteratePrincipalsByRole(realmService.getSystemRealm(), role);
			if(!admins.hasNext()) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.sysAdminRequired");
			}
		}
	}

	@Override
	public void unassignRole(Role role, Principal... principals) throws AccessDeniedException, ResourceException {

		assertAnyPermission(PermissionStrategy.INCLUDE_IMPLIED, RolePermission.UPDATE, RolePermission.UPDATE);

		try {
			
			checkSystemAdministratorAssignments(role);
			if(role.isPersonalRole()) {
				throw new AccessDeniedException("You cannot unassign a personal role from any principal");
			}
			repository.unassignRole(role, principals);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					new ArrayList<Principal>(), Arrays.asList(principals)));
		} catch (Throwable e) {
			eventService
					.publishEvent(new RoleUpdatedEvent(this, role.getName(), e, getCurrentSession(), role.getRealm()));
			throw e;
		}
	}

	@Override
	public Set<Permission> getPrincipalPermissions(Principal principal) {
		return getPrincipalPermissions(getCurrentRealm(), principal);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Permission> getPrincipalPermissions(Realm realm, Principal principal) {
		
		String cacheKey = String.format("%d:::%d", principal.getId(), realm.getId());
		boolean needPerms = false;		
		Set<Permission> permissions = null;
		
		synchronized (permissionsCache) {
			/* We do this up front to only keep the permission cache lock held for a short time */
			if (!CACHE_PERMISSIONS || !permissionsCache.containsKey(cacheKey) || ( permissions = permissionsCache.get(cacheKey))==null) {
				needPerms = true;
			}
		}
		
		if(needPerms) {
			/* Load the roles, make take a while, so do it outside of the lock */
			Set<Role> roles = getPrincipalRoles(principal);
			synchronized (permissionsCache) {
				/* Now check again inside the lock. If things have changed since then, then
				 * we have waste a call to getPrincipalRoles() but at least we won't be holding up other threads
				 */
				if (!permissionsCache.containsKey(cacheKey) || ( permissions = permissionsCache.get(cacheKey))==null) {
					permissions = new HashSet<Permission>();
				
					if(Boolean.getBoolean("hypersocket.debugPermissions")) {
						log.info("{} has {} roles", principal.getName(), roles.size());
					}
					
					for (Role r : roles) {
						if(r == null) {
							log.warn(String.format("NULL role in list of %d roles for %s", roles.size(), principal.getName()));
							continue;
						}
						if(r.getPermissionRealms() == null || !r.getPermissionRealms().contains(realm) || r.getPermissionRealms().size() > 1) {
							/**
							 * This user has roles in other realms than their home realm. This implies
							 * that they can switch realms
							 */
							
							if(Boolean.getBoolean("hypersocket.debugPermissions")) {
								log.info("{} has roles with permissions in other realms. Adding SWITCH_REALM permission", principal.getName());
							}
							
							permissions.add(repository.getPermissionByResourceKey(SystemPermission.SWITCH_REALM.getResourceKey()));
						}
						
						if(r.getPermissionRealms() == null || !r.getPermissionRealms().contains(realm)) {
							if(Boolean.getBoolean("hypersocket.debugPermissions")) {
								log.info("{} has role {} but its not applicable to this realm", principal.getName(), r.getName());
							}
							continue;
						}
						
						if (r.isAllPermissions()) {
							if(Boolean.getBoolean("hypersocket.debugPermissions")) {
								log.info("{} has an all permission role {}", principal.getName(), r.getName());
							}
							permissions.addAll(repository.getAllPermissions(registerPermissionIds, realm.isSystem()));
							break;
						} else {
							if(Boolean.getBoolean("hypersocket.debugPermissions")) {
								for(Permission p : r.getPermissions()) {
									log.info("{} has permission {} from role {}", principal.getName(), p.getResourceKey(), r.getName());
								}
							}
							permissions.addAll(r.getPermissions());
						}
					}
		
					permissionsCache.put(cacheKey, permissions);
				}
			}
		}
		
		if(Boolean.getBoolean("hypersocket.debugPermissions")) {
			for(Permission p : permissions) {
				log.info("{} has permission {}", principal.getName(), p.getResourceKey());
			}
		}
		
		return permissions;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Role> getPrincipalRolesForRealm(Principal principal, Realm realm) {
		
		synchronized(roleCache) {
			String cacheKey = String.format("%d:::%d", principal.getId(), realm.getId());
			if (!roleCache.containsKey(cacheKey)) {
				roleCache.put(cacheKey, repository.getPrincipalRolesForRealm(realmService.getAssociatedPrincipals(principal), realm));
			}
	
			return (Set<Role>) roleCache.get(cacheKey);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Role> getPrincipalRoles(Principal principal) {

		synchronized(roleCache) {
			if (!roleCache.containsKey(principal)) {
				roleCache.put(principal, repository.getRolesForPrincipal(realmService.getAssociatedPrincipals(principal)));
			}
		}

		return (Set<Role>) roleCache.get(principal);
	}

	@Override
	public Set<Role> getPrincipalNonPersonalRoles(Principal principal) {

		Set<Role> roles = getPrincipalRoles(principal);
		CollectionUtils.filter(roles, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				Role r = (Role) o;
				return !r.isPersonalRole();
			}
		});
		return roles;
	}

	@Override
	public Set<Role> getPrincipalNonPersonalNonAllUserRoles(Principal principal) {

		Set<Role> roles = getPrincipalRoles(principal);
		CollectionUtils.filter(roles, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				Role r = (Role) o;
				return !r.isPersonalRole() && !r.isAllUsers();
			}
		});
		return roles;
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

	protected void verifyPermission(Principal principal, PermissionStrategy strategy,
			Set<Permission> principalPermissions, PermissionType... permissions) throws AccessDeniedException {

		if (principal == null) {
			throw new AccessDeniedException();
		}

		if(hasAuthenticatedContext() && principal.equals(getCurrentPrincipal())) {
			if (hasElevatedPermissions()) {
				for (PermissionType perm : getElevatedPermissions()) {
					principalPermissions.add(getPermission(perm.getResourceKey()));
				}
			}
		}

		if (!hasSystemPrincipal(principalPermissions)) {

			Set<PermissionType> derivedPrincipalPermissions = new HashSet<PermissionType>();
			for (Permission t : principalPermissions) {
				if (!registeredPermissions.containsKey(t.getResourceKey())) {
					continue;
				}
				switch (strategy) {
				case INCLUDE_IMPLIED:
					recurseImpliedPermissions(registeredPermissions.get(t.getResourceKey()),
							derivedPrincipalPermissions);
					break;
				case EXCLUDE_IMPLIED:
					derivedPrincipalPermissions.add(registeredPermissions.get(t.getResourceKey()));
					break;
				}

			}

			for (PermissionType t : permissions) {
				if(derivedPrincipalPermissions.contains(t)) {
					return;
				}
			}
			
			Locale currentLocale = null;
			try {
				currentLocale = getCurrentLocale();
			} catch (InvalidAuthenticationContext iac) {
				currentLocale = Locale.getDefault();
			}
			
			throw new AccessDeniedException(
					I18N.getResource(currentLocale, PermissionService.RESOURCE_BUNDLE, "error.accessDenied",
							debugPermissionState(principal, principalPermissions, derivedPrincipalPermissions, permissions)));

		}
	}

	private String debugPermissionState(Principal principal, Set<Permission> principalPermissions,
			Set<PermissionType> derivedPrincipalPermissions, PermissionType... permissions) {
		StringBuilder bui = new StringBuilder();
		bui.append(String.format("This permission failure involved the principal %s (of type %s). The current principal is %s (of type %s)\n",
				principal.getPrincipalName(), principal.getType(), getCurrentPrincipal().getPrincipalName(), getCurrentPrincipal().getType()));
		
		bui.append("The permissions required were (* means have, ! means missing) :-\n");
		for(PermissionType p : permissions) {
			bui.append(String.format("  %sResource Key: %s, Class: %s\n", derivedPrincipalPermissions.contains(p) ? "*" : "!", p.getResourceKey(), p.getClass().getName()));
		}
		
		bui.append("The primary permissions discovered were :-\n");
		for(Permission p : principalPermissions) {
			bui.append(String.format("   Id: %d, Resource Key: %s, Category: %s", p.getId(), p.getResourceKey(), p.getCategory()));
		}
		if(!derivedPrincipalPermissions.isEmpty()) {
			bui.append("The derived permissions discovered were :-\n");
			for(PermissionType p : derivedPrincipalPermissions) {
				bui.append(String.format("  Resource Key: %s, Class: %s\n", p.getResourceKey(), p.getClass().getName()));
			}
		}
		return bui.toString();
	}

	@Override
	@Transactional
	public void verifyPermission(Principal principal, PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		if (principal == null) {
			if (log.isInfoEnabled()) {
				log.info("Denying permission because principal is null");
			}
			throw new AccessDeniedException();
		}
		

		if(hasSystemPermission(principal)) {
			return;
		}

		Set<Permission> principalPermissions = getPrincipalPermissions(principal);

		verifyPermission(principal, strategy, principalPermissions, permissions);
	}
	
	@Override
	public void verifyPermission(Realm realm, Principal principal, PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		if (principal == null) {
			if (log.isInfoEnabled()) {
				log.info("Denying permission because principal is null");
			}
			throw new AccessDeniedException();
		}

		if(hasSystemPermission(principal)) {
			return;
		}
		
		Set<Permission> principalPermissions = getPrincipalPermissions(realm, principal);

		verifyPermission(principal, strategy, principalPermissions, permissions);
	}

	@Override
	public boolean hasSystemPermission(Principal principal) {

		if(principal.getType()==PrincipalType.SYSTEM) {
			return true;
		}
		
		Set<Permission> principalPermissions = getPrincipalPermissions(principal.getRealm(), principal);
		if(hasAuthenticatedContext() && principal.equals(getCurrentPrincipal())) {
			if (hasElevatedPermissions()) {
				for (PermissionType perm : getElevatedPermissions()) {
					principalPermissions.add(getPermission(perm.getResourceKey()));
				}
			}
		}
		return hasSystemPrincipal(principalPermissions);
	}

	@Override
	public boolean hasAdministrativePermission(Principal principal) {

		if(hasSystemPermission(principal)) {
			return true;
		}
		
		Set<Role> roles = getPrincipalRoles(principal);
		for(Role r : roles) {
			if(!r.getPermissionRealms().contains(getCurrentRealm(principal))) {
				continue;
			}
			if(r.isAllPermissions()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasPermission(Principal principal, Permission permission) {

		for (Permission p : getPrincipalPermissions(principal)) {
			if (p.getResourceKey().equals(permission.getResourceKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasAllPermissions(Principal principal, Permission...permissions) {

		final Set<Permission> principalPermissions = getPrincipalPermissions(principal);
		for(final Permission p : permissions) {
			Object result = CollectionUtils.find(principalPermissions, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					Permission permission = (Permission) o;
					return p.getResourceKey().equals(permission.getResourceKey());
				}
			});

			if(result == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasAnyPermission(Principal principal, Permission...permissions) {

		final Set<Permission> principalPermissions = getPrincipalPermissions(principal);
		for(final Permission p : permissions) {
			Object result = CollectionUtils.find(principalPermissions, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					Permission permission = (Permission) o;
					return p.getResourceKey().equals(permission.getResourceKey());
				}
			});

			if(result != null) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasSystemPrincipal(Set<Permission> principalPermissions) {
		for (Permission p : principalPermissions) {
			if (p.getResourceKey().equals(SystemPermission.SYSTEM.getResourceKey())
					|| p.getResourceKey().equals(SystemPermission.SYSTEM_ADMINISTRATION.getResourceKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Role getRole(String name, Realm realm) throws ResourceNotFoundException {

		Role role = repository.getRoleByName(name, realm);
		if (role == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.role.notFound", name);
		}
		return role;
	}

	@Override
	public void deleteRole(Role role) throws AccessDeniedException, ResourceException {
		deleteRole(role, true);
	}
	
	protected void deleteRole(Role role, boolean needEvent) throws AccessDeniedException, ResourceException {
		assertPermission(RolePermission.DELETE);
		try {

			for (AbstractAssignableResourceRepository<?> r : repositories.values()) {
				if (r.getResourceByRoleCount(role.getRealm(), role) > 0) {
					r.removeAssignments(role);
				}
			}

			Collection<Principal> revoked = new ArrayList<Principal>();
			revoked.addAll(role.getPrincipals());
			
			role.getPrincipals().clear();
			role.getPermissions().clear();
			repository.saveRole(role);
			repository.deleteRole(role);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			if(needEvent) {
				eventService.publishEvent(new RoleDeletedEvent(
						this, getCurrentSession(), role.getRealm(), role, revoked));
			}
		} catch (Throwable te) {
			
			if(needEvent) {
				eventService.publishEvent(new RoleDeletedEvent(
						this, role.getName(), te, getCurrentSession(), role.getRealm()));
			}
			throw new ResourceChangeException(te, RESOURCE_BUNDLE, "error.resourceDeleteError", te.getMessage());
		}
	}

	@Override
	public List<Role> allRoles(Realm realm) throws AccessDeniedException {

		assertAnyPermission(RolePermission.READ);

		return repository.getRolesForRealm(realm);
	}

	@Override
	public List<Permission> allPermissions() {
		return repository.getAllPermissions(registerPermissionIds, getCurrentRealm().isSystem());
	}

	private <T> Set<T> getEntitiesNotIn(Collection<T> source, Collection<T> from, EntityMatch<T> validation) {

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
	public void grantPermission(Role role, Permission permission)
			throws AccessDeniedException, ResourceException {

		assertPermission(RolePermission.UPDATE);

		try {
			repository.grantPermission(role, permission);
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					new ArrayList<Principal>(), new ArrayList<Principal>()));
		} catch (Throwable e) {
			eventService
					.publishEvent(new RoleUpdatedEvent(this, role.getName(), e, getCurrentSession(), role.getRealm()));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "error.resourceUpdateError", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Role updateRole(Role role, String name, List<Principal> principals, List<Permission> permissions, List<Realm> realms,
			Map<String, String> properties, boolean allUsers, boolean allPerm, boolean allRealms) throws AccessDeniedException, ResourceException {

		assertPermission(RolePermission.UPDATE);
		if(principals!=null) {
			if(!CollectionUtils.isEqualCollection(role.getPrincipals(), principals)) {
				if(role.isPersonalRole()) {	
					throw new AccessDeniedException("You cannot change personal roles");
				}
			}
		}
		try {
			Role anotherRole = getRole(name, role.getRealm());
			if (!anotherRole.getId().equals(role.getId())) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.role.alreadyExists", name);
			}
		} catch (ResourceNotFoundException ne) {
			role.setName(name);
		}
		try {
			Set<Realm> assignRealms = new HashSet<Realm>();
			Set<Realm> unassignRealms = new HashSet<Realm>();
			
			if(realms!=null) {
				
				unassignRealms.addAll(getEntitiesNotIn(realms, role.getPermissionRealms(),
						new EntityMatch<Realm>() {
							@Override
							public boolean validate(Realm t) {
								return true;
							}
	
				}));
				
				
				assignRealms.addAll(realms);
				assignRealms.removeAll(role.getPermissionRealms());
			}
			
			role.setPermissionRealms(new HashSet<Realm>(realms));
			Set<Principal> assignPrincipals = new HashSet<Principal>();
			Set<Principal> unassignPrincipals = new HashSet<Principal>();
			
			if(principals!=null) {
				unassignPrincipals.addAll(getEntitiesNotIn(principals, role.getPrincipals(),
						new EntityMatch<Principal>() {
							@Override
							public boolean validate(Principal t) {
								return getCurrentRealm().equals(t.getRealm());
							}
	
				}));
				assignPrincipals.addAll(principals);
				assignPrincipals.removeAll(role.getPrincipals());
			}
			
			Set<Permission> revokePermissions = new HashSet<Permission>();
			Set<Permission> grantPermissions = new HashSet<Permission>();
			
			if(permissions!=null) {
				revokePermissions.addAll(getEntitiesNotIn(permissions, role.getPermissions(), null));
				grantPermissions.addAll(getEntitiesNotIn(role.getPermissions(), permissions, null));
			}
			
			role.setAllPermissions(allPerm);
			role.setAllUsers(allUsers);
			role.setAllRealms(allRealms);
			
			repository.updateRole(role, unassignPrincipals, assignPrincipals, revokePermissions, grantPermissions,
					properties, new TransactionAdapter<Role>() {

						@Override
						public void afterOperation(Role resource, Map<String, String> properties) {
							saveRoleAttributes(resource, properties);
						}

					});
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
			eventService.publishEvent(new RoleUpdatedEvent(this, getCurrentSession(), role.getRealm(), role,
					assignPrincipals, unassignPrincipals));
			return role;
		} catch (Throwable te) {
			eventService
					.publishEvent(new RoleUpdatedEvent(this, role.getName(), te, getCurrentSession(), role.getRealm()));
			throw new ResourceChangeException(te, RESOURCE_BUNDLE, "error.resourceUpdateError", te.getMessage());
		}
	}

	@Override
	public Role getRoleById(Long id, Realm realm) throws ResourceNotFoundException, AccessDeniedException {

		assertPermission(RolePermission.READ);

		Role role = repository.getRoleById(id);
		if (role.getRealm() != null && !role.getRealm().equals(realm)) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.invalidRole", id);
		}
		return role;
	}

	@Override
	public Permission getPermissionById(Long id) {
		return repository.getPermissionById(id);
	}

	public class RecursivePrincipalIterator extends ProxiedIterator<Principal> {
		private final Iterator<Principal> principals;
		Iterator<Principal> groupIterator;

		public RecursivePrincipalIterator(Iterator<Principal> principals) {
			this.principals = principals;
		}

		Iterator<Principal> iterateGroups(Principal group) {
			// TODO make get associated principals an iterator
			return new RecursivePrincipalIterator(realmService.getAssociatedPrincipals(group).iterator());
		}

		@Override
		protected Principal checkNext(Principal item) {
			if(item == null) {
				boolean go = false;
				while(go) {
					go = false;
					if(groupIterator != null) {
						if(groupIterator.hasNext()) {
							item = groupIterator.next();
						}
						else
							groupIterator = null;
					}
					
					while(item == null && principals.hasNext()) {
						Principal principal = principals.next();
						if(principal.getType() == PrincipalType.USER) {
							item = principal;
							break;
						}
						else if(principal.getType() == PrincipalType.GROUP) {
							groupIterator = iterateGroups(principal);
							go = true;
							break;
						}
					}
				}
			}
			return item;
		}
	}

	private interface EntityMatch<T> {
		boolean validate(T t);
	}

	@Override
	public Long getRoleCount(String searchPattern, String searchColumn, boolean includeChildRealms, RoleType... types) throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		return repository.countRoles(getCurrentRealm(), searchPattern, searchColumn, includeChildRealms, types);
	}

	@Override
	public List<?> getRoles(String searchPattern, String searchColumn, int start, int length, ColumnSort[] sorting,  boolean includeChildRealms, RoleType... types)
			throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		return repository.searchRoles(getCurrentRealm(), searchPattern, searchColumn, start, length, sorting, includeChildRealms, types);
	}

	@Override
	public List<?> getNoPersonalNoAllUsersRoles(String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		return repository.searchNoPersonalNoAllUserRoles(getCurrentRealm(), searchPattern, start, length, sorting);
	}

	@Override
	public Role getPersonalRole(Principal principal) {
		Role role = repository.getPersonalRole(principal);
		if(role == null)
			role = repository.createPersonalRole(principal);
		return role;
	}

	@Override
	public void onApplicationEvent(SystemEvent event) {

		if (event instanceof GroupEvent || event instanceof UserEvent) {
			synchronized (permissionsCache) {
				permissionsCache.removeAll();
				roleCache.removeAll();
			}
		}

	}

	@Override
	public void revokePermissions(final Principal principal,
			@SuppressWarnings("unchecked") final TransactionAdapter<Principal>... ops)
			throws ResourceException, AccessDeniedException {

		transactionService.doInTransaction(new TransactionCallback<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus status) {

				try {

					long count = repository.getAssignableResourceCount(principal);
					if (count > 0) {
						throw new ResourceException(RESOURCE_BUNDLE, "error.resourcesAssigned",
								principal.getPrincipalName(), count);
					} 

					for (TransactionAdapter<Principal> op : ops) {
						op.beforeOperation(principal, new HashMap<String, String>());
					}

					revokePermissionsNonTransactional(principal);

					for (TransactionAdapter<Principal> op : ops) {
						op.afterOperation(principal, new HashMap<String, String>());
					}
				} catch (Throwable e) {
					throw new IllegalStateException(e.getMessage(), e);
				}

				return principal;
			}

		});

	}
	
	@Override
	public void revokePermissionsNonTransactional(Principal principal) {
		
		Collection<Role> roles = getRolesByPrincipal(principal);
		if(log.isDebugEnabled()) {
			log.debug(String.format("Revoking principal permissioms %s with %d roles [%s]", 
					principal.getPrincipalName(), 
					roles.size(), 
					ResourceUtils.createCommaSeparatedString(roles)));
		}
		HypersocketUtils.memDbg("revokePermissions " + principal.getName());
		
		for(Role role : roles) {
			if(!role.isPersonalRole() && !role.isAllUsers()) {
				role.getPrincipals().remove(principal);
				repository.saveRole(role);
			}
		}
		
		deletePrincipalRole(principal);
		
	}

	@Override
	public boolean hasRole(Principal principal, Role role) {

		for (Role r : getPrincipalRoles(principal)) {
			if (r.equals(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasRole(Principal principal, Collection<Role> roles) {

		Collection<Role> principalRoles = getPrincipalRoles(principal);
		for (Role r : principalRoles) {
			if (roles.contains(r)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasEveryoneRole(Collection<Role> roles, Realm realm) {
		for(Role r : roles)
			if(r.getName().equals(ROLE_EVERYONE))
				return true;
		return false;
	}

	@Override
	public void assertResourceAccess(AssignableResource resource, Principal principal) throws AccessDeniedException {

		boolean found = false;
		Set<Role> principalRoles = getPrincipalRoles(principal);
		for (Role role : resource.getRoles()) {
			if (principalRoles.contains(role)) {
				found = true;
				break;
			}
		}

		if (!found) {
			throw new AccessDeniedException("You do not have access to " + resource.getName());
		}
	}

	/**
	 * Creates role and assigns principals (users) to the newly created role.
	 * 
	 * @param roleName
	 *            role is created with this name
	 * @param realm
	 *            realm to which the role and to be assigned principals belong
	 * @param principals
	 *            list of principals to be added to role created
	 * 
	 * @throws AccessDeniedException
	 * @throws ResourceException
	 * 
	 * @return newly created role instance
	 * 
	 */
	public Role createRoleAndAssignPrincipals(final String roleName, final Realm realm, final Principal... principals)
			throws ResourceException, AccessDeniedException {

		return transactionService.doInTransaction(new TransactionCallback<Role>() {

			@Override
			public Role doInTransaction(TransactionStatus ts) {
				try {
					if (log.isInfoEnabled()) {
						log.info(String.format("Creating role with name %s in realm %s.", roleName, realm.getName()));
					}

					Role role = createRole(roleName, realm, RoleType.CUSTOM);

					assignRole(role, principals);

					return role;
				} catch (AccessDeniedException | ResourceException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}

		});
	}

	@Override
	public String getRoleProperty(Role resource, String resourceKey) {

		return repository.getValue(resource, resourceKey);
	}

	@Override
	public boolean getRoleBooleanProperty(Role resource, String resourceKey) {

		return repository.getBooleanValue(resource, resourceKey);
	}

	@Override
	public Long getRoleLongProperty(Role resource, String resourceKey) {

		return repository.getLongValue(resource, resourceKey);
	}

	@Override
	public int getRoleIntProperty(Role resource, String resourceKey) {

		return repository.getIntValue(resource, resourceKey);
	}

	@Override
	public Collection<PropertyCategory> getRoleTemplate() throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		List<PropertyCategory> cats = new ArrayList<PropertyCategory>();
		cats.addAll(repository.getPropertyCategories(null));
		cats.addAll(attributeService.getPropertyResolver().getPropertyCategories(null));
		return cats;
	}

	@Override
	public Collection<PropertyCategory> getRoleProperties(Role role) throws AccessDeniedException {
		assertPermission(RolePermission.READ);

		List<PropertyCategory> cats = new ArrayList<PropertyCategory>();

		cats.addAll(repository.getPropertyCategories(role));
		cats.addAll(attributeService.getPropertyResolver().getPropertyCategories(role));

		return cats;
	}

	protected void saveRoleAttributes(Role role, Map<String, String> properties) {

		if(properties!=null) {
			for (PropertyTemplate template : attributeService.getPropertyResolver().getPropertyTemplates(role)) {
				if (properties.containsKey(template.getResourceKey())) {
					attributeRepository.setValue(role, template.getResourceKey(),
							properties.get(template.getResourceKey()));
				}
			}
		}
	}

	@Override
	public Set<String> getRolePropertyNames() {
		return attributeRepository.getPropertyNames(null);
	}

	@Override
	public Collection<Role> getRolesByPrincipal(Principal principal) {
		return repository.getRolesForPrincipal(Arrays.asList(principal));
	}
	
	@Override
	public Iterator<Principal> iteratePrincipalsByRole(Realm realm, Role... roles) throws ResourceNotFoundException, AccessDeniedException {
		return iteratePrincipalsByRole(realm, Arrays.asList(roles));
	}
	
	@Override
	public Iterator<Principal> iteratePrincipalsByRole(Realm realm, Collection<Role> roles) {
		if(hasEveryoneRole(roles, realm)) {
			return realmService.iterateUsers(realm);
		}
		return repository.iteratePrincpalsByRole(realm, roles);
	}
	
	@Override
	public Set<Principal> getPrincipalsByRole(Realm realm, int max, Role... roles) throws ResourceNotFoundException, AccessDeniedException {
		return getPrincipalsByRole(realm, max, Arrays.asList(roles));
	}
	
	@Override
	public Set<Principal> getPrincipalsByRole(Realm realm, int max, Collection<Role> roles) throws ResourceNotFoundException {
		if(hasEveryoneRole(roles, realm))
			return realmService.getUsers(realm, max);
		else 
			return repository.getPrincpalsByRole(realm, max, roles);
	}

	protected void deletePrincipalRole(Principal principal) {
		if (principal.isPrimaryAccount()) {
			try {
				Role role = repository.getPersonalRole(principal);
				if (role != null) {
					deleteRole(role, false);
				}
			} catch (ResourceException | AccessDeniedException e) {
				log.error("Failed to delete principal role", e);
			}
		}
	}
	
	@Override
	public Iterator<Principal> resolveUsers(Collection<Role> roles, Realm realm) throws ResourceNotFoundException, AccessDeniedException {
		if(hasEveryoneRole(roles, realm)) {
			return realmService.iterateUsers(realm);
		} else {
			return resolveUsers(iteratePrincipalsByRole(realm, roles));
		}
	}
	@Override
	public Iterator<Principal> resolveUsers(Iterator<Principal> principals) {
		return new RecursivePrincipalIterator(principals);
	}
	
	@Override
	public boolean hasPermission(Principal principal, PermissionType permission) {
		return hasPermission(principal, getPermission(permission.getResourceKey()));
	}

	@Override
	public void deleteResources(final List<Role> resources) throws ResourceException, AccessDeniedException {
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				for (Role role : resources) {
					try {
						deleteRole(role);
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
				return null;
			}
		});
	}

	@Override
	public List<Role> getResourcesByIds(Long... ids) throws AccessDeniedException {
		return repository.getResourcesByIds(ids);
	}

	public Role getRealmAdministratorRole(Realm realm) {
		return repository.getRoleByName(ROLE_REALM_ADMINISTRATOR, realm);
	}
	
	@Override
	public Role getSystemAdministratorRole() {
		return repository.getRoleByName(ROLE_SYSTEM_ADMINISTRATOR, realmService.getSystemRealm());
	}

	@Override
	public Role getRoleById(Long id) {
		return repository.getResourceById(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Realm> getPrincipalPermissionRealms(Principal principal) {
		
		String cacheKey = String.format("%d:::permissionRealms", principal.getId());
		
		synchronized(roleCache) {
			if (!roleCache.containsKey(cacheKey)) {
				Set<Realm> realms = new HashSet<Realm>();
				for(Role r : repository.getRolesForPrincipal(realmService.getAssociatedPrincipals(principal))) {
					realms.addAll(r.getPermissionRealms());
				}
				roleCache.put(cacheKey, realms);
			}
	
			return (Set<Realm>) roleCache.get(cacheKey);
		}
	}

	@Override
	public Set<Role> getAllUserRoles() {
		return repository.getAllUserRoles(getCurrentRealm());
	}

	@Override
	public void assertAdministrativeAccess() throws AccessDeniedException {
		assertRole(getAllPermissionsRoles().toArray(new Role[0]));
	}

	private Collection<Role> getAllPermissionsRoles() {
		return repository.getAllPermissionsRoles(getCurrentRealm());
	}

	@Override
	public Set<Role> getPersonalRoles(Realm realm) {
		return repository.getPersonalRoles(realm);
	}
	
}