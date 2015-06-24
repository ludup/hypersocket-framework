/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.events.ChangePasswordEvent;
import com.hypersocket.realm.events.GroupCreatedEvent;
import com.hypersocket.realm.events.GroupDeletedEvent;
import com.hypersocket.realm.events.GroupEvent;
import com.hypersocket.realm.events.GroupUpdatedEvent;
import com.hypersocket.realm.events.PrincipalEvent;
import com.hypersocket.realm.events.ProfileUpdatedEvent;
import com.hypersocket.realm.events.RealmCreatedEvent;
import com.hypersocket.realm.events.RealmDeletedEvent;
import com.hypersocket.realm.events.RealmEvent;
import com.hypersocket.realm.events.RealmUpdatedEvent;
import com.hypersocket.realm.events.SetPasswordEvent;
import com.hypersocket.realm.events.UserCreatedEvent;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.realm.events.UserUpdatedEvent;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class RealmServiceImpl extends PasswordEnabledAuthenticatedServiceImpl implements
		RealmService, UpgradeServiceListener {

	static Logger log = LoggerFactory.getLogger(RealmServiceImpl.class);

	Map<String, RealmProvider> providersByModule = new HashMap<String, RealmProvider>();

	List<RealmListener> realmListeners = new ArrayList<RealmListener>();

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	Principal systemPrincipal;

	Realm systemRealm;

	@Autowired
	UpgradeService upgradeService;

	@Autowired
	SessionService sessionService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	PrincipalSuspensionService suspensionService;

	@Autowired
	UserVariableReplacement userVariableReplacement;
	
	CacheManager cacheManager;
	Cache realmCache;

	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.realms");

		for (RealmPermission p : RealmPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE,
				"category.acl");

		for (UserPermission p : UserPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (ProfilePermission p : ProfilePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (GroupPermission p : GroupPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (RolePermission p : RolePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE,
				"category.password");

		for (PasswordPermission p : PasswordPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		eventService.registerEvent(RealmEvent.class, RESOURCE_BUNDLE,
				new RealmPropertyCollector());
		eventService.registerEvent(RealmCreatedEvent.class, RESOURCE_BUNDLE,
				new RealmPropertyCollector());
		eventService.registerEvent(RealmUpdatedEvent.class, RESOURCE_BUNDLE,
				new RealmPropertyCollector());
		eventService.registerEvent(RealmDeletedEvent.class, RESOURCE_BUNDLE,
				new RealmPropertyCollector());

		eventService.registerEvent(PrincipalEvent.class, RESOURCE_BUNDLE);

		eventService.registerEvent(UserEvent.class, RESOURCE_BUNDLE,
				new UserPropertyCollector());
		eventService.registerEvent(UserCreatedEvent.class, RESOURCE_BUNDLE,
				new UserPropertyCollector());
		eventService.registerEvent(UserUpdatedEvent.class, RESOURCE_BUNDLE,
				new UserPropertyCollector());
		eventService.registerEvent(UserDeletedEvent.class, RESOURCE_BUNDLE,
				new UserPropertyCollector());

		eventService.registerEvent(GroupEvent.class, RESOURCE_BUNDLE,
				new GroupPropertyCollector());
		eventService.registerEvent(GroupCreatedEvent.class, RESOURCE_BUNDLE,
				new GroupPropertyCollector());
		eventService.registerEvent(GroupUpdatedEvent.class, RESOURCE_BUNDLE,
				new GroupPropertyCollector());
		eventService.registerEvent(GroupDeletedEvent.class, RESOURCE_BUNDLE,
				new GroupPropertyCollector());

		eventService.registerEvent(ProfileUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ChangePasswordEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SetPasswordEvent.class, RESOURCE_BUNDLE);

		upgradeService.registerListener(this);

		cacheManager = CacheManager.newInstance();
		realmCache = new Cache("realmCache", 5000, false, false, 60 * 60,
				60 * 60);
		cacheManager.addCache(realmCache);
	}

	@Override
	public void onUpgradeComplete() {

		setCurrentPrincipal(getSystemPrincipal(), Locale.getDefault(),
				getSystemPrincipal().getRealm());

		try {
			for (Realm realm : realmRepository.allRealms()) {
				for (RealmListener listener : realmListeners) {
					if (!listener.hasCreatedDefaultResources(realm)) {
						listener.onCreateRealm(realm);
					}
				}
			}
		} finally {
			clearPrincipalContext();
		}
	}

	@Override
	public List<RealmProvider> getProviders() throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		return new ArrayList<RealmProvider>(providersByModule.values());
	}

	@Override
	public RealmProvider getProviderForRealm(Realm realm) {
		return getProviderForRealm(realm.getResourceCategory());
	}

	@Override
	public RealmProvider getProviderForRealm(String module) {
		if (!providersByModule.containsKey(module))
			throw new IllegalArgumentException(
					"No provider available for realm module " + module);
		return providersByModule.get(module);
	}

	protected boolean hasProviderForRealm(Realm realm) {
		return providersByModule.containsKey(realm.getResourceCategory());
	}

	@Override
	public List<Principal> allUsers(Realm realm) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ,
				RealmPermission.READ);

		return allPrincipals(realm, PrincipalType.USER);
	}

	@Override
	public List<Principal> allGroups(Realm realm) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ,
				RealmPermission.READ);

		return allPrincipals(realm, PrincipalType.GROUP);
	}

	protected List<Principal> allPrincipals(Realm realm, PrincipalType... types) {
		if (types.length == 0) {
			types = PrincipalType.ALL_TYPES;
		}
		return getProviderForRealm(realm).allPrincipals(realm, types);
	}

	@Override
	public void registerRealmProvider(RealmProvider provider) {

		if (log.isInfoEnabled()) {
			log.info("Registering " + provider.getModule() + " realm provider");
		}
		providersByModule.put(provider.getModule(), provider);
	}

	@Override
	public void unregisterRealmProvider(RealmProvider provider) {

		if (log.isInfoEnabled()) {
			log.info("Unregistering " + provider.getModule()
					+ " realm provider");
		}
		providersByModule.remove(provider.getModule());
	}

	@Override
	public Realm getRealmByName(String realm) throws AccessDeniedException {
		return realmRepository.getRealmByName(realm);
	}

	@Override
	public boolean isRegistered(RealmProvider provider) {
		return providersByModule.containsKey(provider.getModule());
	}

	@Override
	public String[] getRealmPropertyArray(Realm realm, String resourceKey) {
		return getRealmProperty(realm, resourceKey).split("\\]\\|\\[");
	}

	@Override
	public String getRealmProperty(Realm realm, String resourceKey) {

		RealmProvider provider = getProviderForRealm(realm);
		return provider.getValue(realm, resourceKey);
	}

	@Override
	public int getRealmPropertyInt(Realm realm, String resourceKey) {
		return Integer.parseInt(getRealmProperty(realm, resourceKey));
	}

	@Override
	public Realm getRealmByHost(String host) {
		return getRealmByHost(host, getDefaultRealm());
	}
	
	@Override
	public Realm getRealmByHost(String host, Realm defaultRealm) {

		if(StringUtils.isBlank(host)) {
			return defaultRealm;
		}
		
		if (!realmCache.isElementInMemory(host)
				|| (realmCache.get(host) == null || realmCache
						.isExpired(realmCache.get(host)))) {
			for (Realm r : internalAllRealms()) {
				RealmProvider provider = getProviderForRealm(r);
				String[] realmHosts = provider.getValues(r, "realm.host");
				for (String realmHost : realmHosts) {
					if (realmHost != null && !"".equals(realmHost)) {
						if (realmHost.equalsIgnoreCase(host)) {
							realmCache.put(new Element(host, r));
							return r;
						}
					}
				}
			}
			return defaultRealm;
		}

		return (Realm) realmCache.get(host).getObjectValue();
	}

	@Override
	public String getRealmHostname(Realm realm) {
		RealmProvider provder = getProviderForRealm(realm);
		String[] names = provder.getValues(realm, "realm.host");
		if (names.length > 0) {
			return names[0];
		}
		return "";
	}

	@Override
	public Realm getRealmById(Long id) throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		return realmRepository.getRealmById(id);
	}

	@Override
	public Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals,
			String password, boolean forceChange)
			throws ResourceCreationException, AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {

			assertAnyPermission(UserPermission.CREATE, RealmPermission.CREATE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			Principal principal = provider.createUser(realm, username,
					properties, principals, password, forceChange);

			eventService.publishEvent(new UserCreatedEvent(this,
					getCurrentSession(), realm, provider, principal,
					principals, properties));

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserCreatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(new UserCreatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(new UserCreatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.unexpectedError", e.getMessage());
		}

	}

	@Override
	public Principal updateUser(Realm realm, Principal user, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceChangeException, AccessDeniedException {

		final RealmProvider provider = getProviderForRealm(realm);

		try {

			assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			Principal principal = provider.updateUser(realm, user, username,
					properties, principals);

			eventService.publishEvent(new UserUpdatedEvent(this,
					getCurrentSession(), realm, provider, principal,
					principals, properties));

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, username, properties,
					principals));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"updateUser.unexpectedError", e.getMessage());
		}
	}

	@Override
	public boolean verifyPrincipal(Principal principal) {
		return !principal.isSuspended();
	}

	@Override
	public boolean verifyPassword(Principal principal, char[] password) {

		/**
		 * Adds support for session tokens. These can be created and used
		 * instead of passwords where we may not have, or want to distribute the
		 * password to an external service.
		 */
		String pwd = new String(password);
		if (pwd.startsWith(SessionServiceImpl.TOKEN_PREFIX)) {
			Principal tokenPrincipal = sessionService.getSessionTokenResource(
					pwd, Principal.class);
			return tokenPrincipal.equals(principal);
		} else {
			return getProviderForRealm(principal.getRealm()).verifyPassword(
					principal, password);
		}
	}

	@Override
	public Principal getPrincipalByName(Realm realm, String principalName,
			PrincipalType... type) {
		if (type.length == 0) {
			type = PrincipalType.ALL_TYPES;
		}
		if(realm==null) {
			try {
				return getUniquePrincipal(principalName, type);
			} catch (ResourceNotFoundException e) {
				return null;
			}
		} else {
			return getProviderForRealm(realm).getPrincipalByName(principalName,
					realm, type);	
		}
	}

	@Override
	public void deleteRealm(String name) throws ResourceChangeException,
			ResourceNotFoundException, AccessDeniedException {

		assertPermission(RealmPermission.DELETE);

		Realm realm = getRealmByName(name);

		if (realm == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE,
					"error.invalidRealm", name);
		}

		deleteRealm(realm);
	}

	private boolean hasSystemAdministrator(Realm r) {

		Set<Principal> sysAdmins = permissionService
				.getUsersWithPermissions(SystemPermission.SYSTEM_ADMINISTRATION);
		for (Principal p : sysAdmins) {
			if (p.getRealm().equals(r)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Realm> allRealms() throws AccessDeniedException {
		assertPermission(RealmPermission.READ);

		return filterRealms(null, false);
	}

	@Override
	public List<Realm> allRealms(boolean ignoreMissingProvider)
			throws AccessDeniedException {
		assertPermission(RealmPermission.READ);
		return filterRealms(null, ignoreMissingProvider);
	}

	private List<Realm> internalAllRealms() {
		return filterRealms(null, false);
	}

	private List<Realm> filterRealms(Class<? extends RealmProvider> clz,
			boolean ignoreMissingProvider) {

		List<Realm> realms = realmRepository.allRealms();
		List<Realm> ret = new ArrayList<Realm>(realms);
		for (Realm r : realms) {
			if (!ignoreMissingProvider) {
				if (!hasProviderForRealm(r)) {
					ret.remove(r);
					continue;
				}
				if (clz != null
						&& !clz.isAssignableFrom(getProviderForRealm(r)
								.getClass())) {
					ret.remove(r);
				}
			}
		}
		return ret;
	}

	@Override
	public List<Realm> allRealms(Class<? extends RealmProvider> clz) {
		return filterRealms(clz, false);
	}

	@Override
	public void changePassword(Principal principal, String oldPassword,
			String newPassword) throws ResourceCreationException,
			ResourceChangeException, AccessDeniedException {
		assertPermission(PasswordPermission.CHANGE);
		RealmProvider provider = getProviderForRealm(principal.getRealm());

		try {
			if (!verifyPassword(principal, oldPassword.toCharArray())) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.invalidPassword");
			}

			provider.changePassword(principal, oldPassword.toCharArray(),
					newPassword.toCharArray());

			setCurrentPassword(newPassword);

			eventService.publishEvent(new ChangePasswordEvent(this,
					getCurrentSession(), getCurrentRealm(), provider));

		} catch (ResourceException ex) {
			eventService.publishEvent(new ChangePasswordEvent(this, ex,
					getCurrentSession(), getCurrentRealm(), provider));
			throw ex;
		} catch (Throwable t) {
			eventService.publishEvent(new ChangePasswordEvent(this, t,
					getCurrentSession(), getCurrentRealm(), provider));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.unexpectedError", t.getMessage());
		}
	}

	@Override
	public void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		try {

			if (provider.isReadOnly(principal.getRealm())) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}

			provider.setPassword(principal, password.toCharArray(),
					forceChangeAtNextLogon);

			eventService
					.publishEvent(new SetPasswordEvent(this,
							getCurrentSession(), getCurrentRealm(), provider,
							principal));

		} catch (ResourceCreationException ex) {
			eventService.publishEvent(new SetPasswordEvent(this, ex,
					getCurrentSession(), getCurrentRealm(), provider, principal
							.getPrincipalName()));
			throw ex;
		}

	}

	@Override
	public boolean isReadOnly(Realm realm) {

		RealmProvider provider = getProviderForRealm(realm);
		return provider.isReadOnly(realm);
	}

	@Override
	public Realm getSystemRealm() {
		if (systemRealm == null) {
			systemRealm = realmRepository.getRealmByName(SYSTEM_REALM);
		}
		return systemRealm;
	}

	@Override
	public Principal getSystemPrincipal() {
		if (systemPrincipal == null) {
			systemPrincipal = getPrincipalByName(
					realmRepository.getRealmByName(SYSTEM_REALM),
					SYSTEM_PRINCIPAL, PrincipalType.SYSTEM);
		}
		return systemPrincipal;
	}

	@Override
	public Realm createRealm(String name, String module,
			Map<String, String> properties) throws AccessDeniedException,
			ResourceCreationException {

		try {
			assertPermission(RealmPermission.CREATE);

			if (realmRepository.getRealmByName(name) != null) {
				ResourceCreationException ex = new ResourceCreationException(
						RESOURCE_BUNDLE, "error.nameAlreadyExists", name);
				eventService.publishEvent(new RealmCreatedEvent(this, ex,
						getCurrentSession(), name, module));
				throw ex;
			}

			RealmProvider realmProvider = getProviderForRealm(module);

			realmProvider.testConnection(properties, true);

			Realm realm = realmRepository.createRealm(name, module, properties,
					realmProvider);

			fireRealmCreate(realm);

			eventService.publishEvent(new RealmCreatedEvent(this,
					getCurrentSession(), realm));

			return realm;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmCreatedEvent(this, e,
					getCurrentSession(), name, module));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(new RealmCreatedEvent(this, e,
					getCurrentSession(), name, module));
			throw e;
		} catch (Throwable t) {
			eventService.publishEvent(new RealmCreatedEvent(this, t,
					getCurrentSession(), name, module));
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.genericError", name, t.getMessage());
		}
	}

	private void clearCache(Realm realm) {
		RealmProvider realmProvider = getProviderForRealm(realm
				.getResourceCategory());

		String[] hosts = realmProvider.getValues(realm, "realm.host");
		for (String host : hosts) {
			realmCache.remove(host);
		}
	}
	
	@Override
	public void setRealmProperty(Realm realm, String resourceKey, String value) throws AccessDeniedException {
		
		assertPermission(RealmPermission.UPDATE);
		RealmProvider realmProvider = getProviderForRealm(realm
				.getResourceCategory());
		
		realmProvider.setValue(realm, resourceKey, value);
	}
	


	@Override
	public Realm updateRealm(Realm realm, String name,
			Map<String, String> properties) throws AccessDeniedException,
			ResourceChangeException {

		try {

			assertPermission(RealmPermission.UPDATE);

			if (!realm.getName().equals(name)) {
				if (realmRepository.getRealmByName(name) != null) {
					throw new ResourceChangeException(RESOURCE_BUNDLE,
							"error.nameAlreadyExists", name);
				}
			}

			RealmProvider realmProvider = getProviderForRealm(realm
					.getResourceCategory());

			realmProvider.testConnection(properties, false);
			String oldName = realm.getName();

			clearCache(realm);

			realm.setName(name);

			realm = realmRepository.saveRealm(realm, properties,
					getProviderForRealm(realm));

			fireRealmUpdate(realm);

			eventService.publishEvent(new RealmUpdatedEvent(this,
					getCurrentSession(), oldName, realmRepository
							.getRealmById(realm.getId())));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e,
					getCurrentSession(), realm));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e,
					getCurrentSession(), realm));
			throw e;
		} catch (Throwable t) {
			eventService.publishEvent(new RealmUpdatedEvent(this, t,
					getCurrentSession(), realm));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.unexpectedError", t);
		}
		return realm;
	}

	private void fireRealmUpdate(Realm realm) {

		for (RealmListener l : realmListeners) {
			try {
				l.onUpdateRealm(realm);
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	private void fireRealmCreate(Realm realm) {

		for (RealmListener l : realmListeners) {
			try {
				l.onCreateRealm(realm);
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	private void fireRealmDelete(Realm realm) {

		for (RealmListener l : realmListeners) {
			try {
				l.onDeleteRealm(realm);
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	@Override
	public void deleteRealm(Realm realm) throws AccessDeniedException,
			ResourceChangeException {

		try {

			assertPermission(RealmPermission.DELETE);

			List<Realm> realms = realmRepository.allRealms();
			if (realms.size() == 1) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.zeroRealms", realm.getName());
			}

			realms.remove(realm);

			if (hasSystemAdministrator(realm)) {
				boolean systemAdministratorPresent = false;

				for (Realm r : realms) {
					if (hasSystemAdministrator(r)) {
						systemAdministratorPresent = true;
						break;
					}
				}

				if (!systemAdministratorPresent) {
					throw new ResourceChangeException(RESOURCE_BUNDLE,
							"error.zeroSysAdmins", realm.getName());
				}
			}

			clearCache(realm);
			
			fireRealmDelete(realm);

			realmRepository.delete(realm);

			eventService.publishEvent(new RealmDeletedEvent(this,
					getCurrentSession(), realm));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmDeletedEvent(this, e,
					getCurrentSession(), realm));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new RealmDeletedEvent(this, e,
					getCurrentSession(), realm));
			throw e;
		} catch (Throwable t) {
			eventService.publishEvent(new RealmDeletedEvent(this, t,
					getCurrentSession(), realm));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.unexpectedError", t);
		}
	}

	@Override
	public Realm setDefaultRealm(Realm realm) throws AccessDeniedException {
		assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);

		return realmRepository.setDefaultRealm(realm);
	}

	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(Realm realm)
			throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);

		return provider.getRealmProperties(realm);

	}

	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(String module)
			throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getRealmProperties(null);
	}

	@Override
	public Principal getPrincipalById(Realm realm, Long id,
			PrincipalType... type) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ,
				RealmPermission.READ);

		if (type.length == 0) {
			type = PrincipalType.ALL_TYPES;
		}
		return getProviderForRealm(realm).getPrincipalById(id, realm, type);
	}

	@Override
	public boolean requiresPasswordChange(Principal principal, Realm realm) {
		return getProviderForRealm(realm).requiresPasswordChange(principal);
	}

	@Override
	public Principal createGroup(Realm realm, String name,
			Map<String, String> properties, List<Principal> principals) throws ResourceCreationException,
			AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {
			assertAnyPermission(GroupPermission.CREATE, RealmPermission.CREATE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			Principal group = getPrincipalByName(realm, name,
					PrincipalType.GROUP);

			if (group != null) {
				ResourceCreationException ex = new ResourceCreationException(
						RESOURCE_BUNDLE, "error.group.alreadyExists", name);
				throw ex;
			}
			
			Principal principal = provider.createGroup(realm, name, properties, principals);
			
			eventService.publishEvent(new GroupCreatedEvent(this,
					getCurrentSession(), realm, provider, principal,
					principals, new HashMap<String, String>()));
			return principal;

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new GroupCreatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(new GroupCreatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(new GroupCreatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"createGroup.unexpectedError", e.getMessage());
		}
	}

	@Override
	public Principal updateGroup(Realm realm, Principal group, String name,
			Map<String, String> properties, List<Principal> principals) throws ResourceChangeException,
			AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {

			assertAnyPermission(GroupPermission.UPDATE, RealmPermission.UPDATE);
			
			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			Principal tmpGroup = getPrincipalByName(realm, name,
					PrincipalType.GROUP);

			if (tmpGroup != null && !tmpGroup.getId().equals(group.getId())) {
				ResourceChangeException ex = new ResourceChangeException(
						RESOURCE_BUNDLE, "error.group.alreadyExists", name);
				throw ex;
			}

			Principal principal = provider.updateGroup(realm, group, name,
					properties, principals);

			eventService.publishEvent(new GroupUpdatedEvent(this,
					getCurrentSession(), realm, provider, principal,
					principals, new HashMap<String, String>()));

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new GroupUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new GroupUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(new GroupUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, name, principals));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"groupUser.unexpectedError", e.getMessage());
		}

	}

	@Override
	public void deleteGroup(Realm realm, Principal group)
			throws ResourceChangeException, AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {
			assertAnyPermission(GroupPermission.DELETE, RealmPermission.DELETE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			provider.deleteGroup(group);

			eventService.publishEvent(new GroupDeletedEvent(this,
					getCurrentSession(), realm, provider, group,
					new HashMap<String, String>()));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e,
					getCurrentSession(), realm, provider, group
							.getPrincipalName()));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e,
					getCurrentSession(), realm, provider, group
							.getPrincipalName()));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e,
					getCurrentSession(), realm, provider, group
							.getPrincipalName()));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"deleteGroup.unexpectedError", e.getMessage());
		}
	}

	@Override
	public void deleteUser(Realm realm, Principal user)
			throws ResourceChangeException, AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {
			assertAnyPermission(UserPermission.DELETE, RealmPermission.DELETE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.realmIsReadOnly");
			}
			
			if (permissionService.hasSystemPermission(user)) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.cannotDeleteSystemAdmin",
						user.getPrincipalName());
			}

			provider.deleteUser(user);

			eventService.publishEvent(new UserDeletedEvent(this,
					getCurrentSession(), realm, provider, user));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserDeletedEvent(this, e,
					getCurrentSession(), realm, provider, user
							.getPrincipalName()));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new UserDeletedEvent(this, e,
					getCurrentSession(), realm, provider, user
							.getPrincipalName()));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(new UserDeletedEvent(this, e,
					getCurrentSession(), realm, provider, user
							.getPrincipalName()));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"deleteUser.unexpectedError", e.getMessage());
		}

	}

	@Override
	public String getPrincipalAddress(Principal principal, MediaType type)
			throws MediaNotFoundException {

		RealmProvider provider = getProviderForRealm(principal.getRealm());
		return provider.getAddress(principal, type);
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(
			Principal principal) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ,
				RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		return provider.getUserProperties(principal);
	}

	@Override
	public Collection<PropertyCategory> getUserProfileTemplates(
			Principal principal) throws AccessDeniedException {

		assertAnyPermission(ProfilePermission.READ);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		Collection<PropertyCategory> ret = provider
				.getUserProperties(principal);

		Set<String> editable = new HashSet<String>(
				Arrays.asList(getRealmPropertyArray(principal.getRealm(),
						"realm.userEditableProperties")));
		Set<String> visible = new HashSet<String>(
				Arrays.asList(getRealmPropertyArray(principal.getRealm(),
						"realm.userVisibleProperties")));

		/**
		 * Filter the properties down to read only and editable as defined by
		 * the realm configuration.
		 */
		for (PropertyCategory c : ret) {

			if(c.isUserCreated()) {
				continue;
			}
			
			List<AbstractPropertyTemplate> tmp = new ArrayList<AbstractPropertyTemplate>();
			for (AbstractPropertyTemplate t : c.getTemplates()) {

				if(!editable.contains(t.getResourceKey())) {
					if (!visible.contains(t.getResourceKey())) {
						tmp.add(t);
						continue;
					}
					t.setReadOnly(true);
					continue;
				}
			}

			c.getTemplates().removeAll(tmp);
		}

		return ret;
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(String module)
			throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ,
				RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getUserProperties(null);
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates()
			throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ,
				RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(getCurrentRealm());

		return provider.getUserProperties(null);
	}

	@Override
	public Collection<String> getUserPropertyNames(Realm realm, Principal principal)
			throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ,
				RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);

		return provider.getUserPropertyNames(principal);

	}

	@Override
	public Collection<String> getUserPropertyNames(String module)
			throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getUserPropertyNames(null);
	}

	@Override
	public Collection<PropertyCategory> getGroupPropertyTemplates(String module)
			throws AccessDeniedException {

		assertAnyPermission(GroupPermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getGroupProperties(null);
	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal) {

		List<Principal> result = getProviderForRealm(principal.getRealm())
				.getAssociatedPrincipals(principal);
		if (!result.contains(principal)) {
			result.add(principal);
		}
		return result;
	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal,
			PrincipalType type) {

		return getProviderForRealm(principal.getRealm())
				.getAssociatedPrincipals(principal, type);
	}

	@Override
	public List<?> getPrincipals(Realm realm, PrincipalType type,
			String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ,
				ProfilePermission.READ, RealmPermission.READ);

		return getProviderForRealm(realm).getPrincipals(realm, type,
				searchPattern, start, length, sorting);
	}

	@Override
	public Long getPrincipalCount(Realm realm, PrincipalType type,
			String searchPattern) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ,
				ProfilePermission.READ, RealmPermission.READ);

		return getProviderForRealm(realm).getPrincipalCount(realm, type,
				searchPattern);
	}

	@Override
	public boolean findUniquePrincipal(String user) {

		int found = 0;
		for (Realm r : internalAllRealms()) {
			Principal p = getPrincipalByName(r, user, PrincipalType.USER);
			if (p != null) {
				found++;
			}
		}
		return found == 1;
	}

	@Override
	public Principal getUniquePrincipal(String username, PrincipalType...  type)
			throws ResourceNotFoundException {
		int found = 0;
		Principal ret = null;
		for (Realm r : internalAllRealms()) {
			Principal p = getProviderForRealm(r).getPrincipalByName(username, r, type);	
			if (p != null) {
				ret = p;
				found++;
			}
		}
		if (found != 1) {
			if(found > 1 && log.isInfoEnabled()) {
				log.info("More than one principal found for username " + username);
			}
			throw new ResourceNotFoundException(RESOURCE_BUNDLE,
					"principal.notFound");
		}
		return ret;
	}

	@Override
	public List<Realm> getRealms(String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		return realmRepository.searchRealms(searchPattern, start, length,
				sorting);
	}

	@Override
	public Long getRealmCount(String searchPattern)
			throws AccessDeniedException {

		assertPermission(RealmPermission.READ);

		return realmRepository.countRealms(searchPattern);
	}

	@Override
	public void updateProfile(Realm realm, Principal principal,
			Map<String, String> properties) throws AccessDeniedException,
			ResourceChangeException {

		RealmProvider provider = getProviderForRealm(realm);

		/**
		 * This ensures we only ever update those properties that are allowed
		 */
		String[] editableProperties = getRealmPropertyArray(realm,
				"realm.userEditableProperties");

		Map<String, String> currentProperties = provider.getUserPropertyValues(principal);
		
		for (String allowed : editableProperties) {
			if (properties.containsKey(allowed)) {
				currentProperties.put(allowed, properties.get(allowed));
			} 
		}

		try {
			assertAnyPermission(ProfilePermission.UPDATE,
					RealmPermission.UPDATE, UserPermission.UPDATE);

			List<Principal> assosiated = provider
					.getAssociatedPrincipals(principal);

			principal = provider.updateUser(realm, principal,
					principal.getPrincipalName(), currentProperties,
					assosiated);

			eventService.publishEvent(new ProfileUpdatedEvent(this,
					getCurrentSession(), realm, provider, principal,
					currentProperties));
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new ProfileUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, principal
							.getPrincipalName(), currentProperties));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new ProfileUpdatedEvent(this, e,
					getCurrentSession(), realm, provider, principal
							.getPrincipalName(), currentProperties));
			throw e;
		}

	}

	@Override
	public String getPrincipalDescription(Principal principal) {

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		return provider.getPrincipalDescription(principal);
	}

	@Override
	public boolean supportsAccountUnlock(Realm realm) {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.supportsAccountUnlock(realm);
	}

	@Override
	public boolean supportsAccountDisable(Realm realm) {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.supportsAccountDisable(realm);
	}

	@Override
	public Principal disableAccount(Principal principal)
			throws ResourceChangeException, AccessDeniedException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		if (provider.isReadOnly(principal.getRealm())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.realmIsReadOnly");
		}
		
		return provider.disableAccount(principal);

	}

	@Override
	public Principal enableAccount(Principal principal)
			throws ResourceChangeException, AccessDeniedException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);
		
		RealmProvider provider = getProviderForRealm(principal.getRealm());

		if (provider.isReadOnly(principal.getRealm())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.realmIsReadOnly");
		}
		
		return provider.enableAccount(principal);
	}

	@Override
	public Principal unlockAccount(Principal principal)
			throws ResourceChangeException, AccessDeniedException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		if (provider.isReadOnly(principal.getRealm())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.realmIsReadOnly");
		}
		
		return provider.unlockAccount(principal);
	}

	@Override
	public void registerRealmListener(RealmListener listener) {
		realmListeners.add(listener);
	}

	@Override
	public Realm getDefaultRealm() {
		return realmRepository.getDefaultRealm();
	}

	class RealmPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getPropertyNames(null);
		}

	}

	class UserPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getUserPropertyNames(null);
		}
	}

	class GroupPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getGroupPropertyNames(null);
		}

	}

	@Override
	public boolean isRealmStrictedToHost(Realm realm) {

		if(realm==null) {
			return false;
		}
		RealmProvider realmProvider = getProviderForRealm(realm);
		return realmProvider.getBooleanValue(realm, "realm.hostRestriction");

	}

	@Override
	public Collection<String> getUserVariableNames(Realm realm, Principal principal) {

		RealmProvider provider = getProviderForRealm(realm);

		Set<String> tmp = new HashSet<String>(
				UserVariableReplacementImpl.getDefaultReplacements());
		tmp.addAll(provider.getUserVariableNames(principal));
		return tmp;

	}

	@Override
	public String getPrincipalEmail(Principal principal) {
		try {
			return getPrincipalAddress(principal, MediaType.EMAIL);
		} catch (MediaNotFoundException e) {
			return "";
		}
	}

	@Override
	public String getPrincipalPhone(Principal principal) {
		try {
			return getPrincipalAddress(principal, MediaType.PHONE);
		} catch (MediaNotFoundException e) {
			return "";
		}
	}

	@Override
	public Map<String, String> getUserPropertyValues(Principal principal,
			String... variableNames) {

		Map<String, String> variables = new HashMap<String, String>();

		for (String variableName : variableNames) {
			variables.put(variableName,
					userVariableReplacement.getVariableValue(principal, variableName));
		}

		return variables;
	}
}
