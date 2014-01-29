/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.events.RealmCreatedEvent;
import com.hypersocket.realm.events.RealmDeletedEvent;
import com.hypersocket.realm.events.RealmUpdatedEvent;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

@Service
public class RealmServiceImpl extends AuthenticatedServiceImpl implements
		RealmService {

	Map<String, RealmProvider> providersByModule = new HashMap<String, RealmProvider>();

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	Principal systemPrincipal;

	@PostConstruct
	public void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.realms");

		for (RealmPermission p : RealmPermission.values()) {
			permissionService.registerPermission(p.getResourceKey(), cat);
		}

		cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE,
				"category.acl");

		for (AccessControlPermission p : AccessControlPermission.values()) {
			permissionService.registerPermission(p.getResourceKey(), cat);
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
	
	private RealmProvider getProviderForRealm(String module) {
		if (!providersByModule.containsKey(module))
			throw new IllegalArgumentException(
					"No provider available for realm module "
							+ module);
		return providersByModule.get(module);
	}

	protected boolean hasProviderForRealm(Realm realm) {
		return providersByModule.containsKey(realm.getResourceCategory());
	}

	@Override
	public List<Principal> allPrincipals(Realm realm, PrincipalType... types) {
		if (types.length == 0) {
			types = PrincipalType.ALL_TYPES;
		}
		return getProviderForRealm(realm).allPrincipals(realm, types);
	}

	@Override
	public void registerRealmProvider(RealmProvider provider) {
		providersByModule.put(provider.getModule(), provider);
//		PropertyCategory category = realmRepository.registerPropertyCategory(
//				"realm.server", RealmService.RESOURCE_BUNDLE,
//				provider.getModule());
//		realmRepository.registerPropertyTemplate(category, "realm.host",
//				"{ \"inputType\": \"text\" }", 0, "");
	}

	@Override
	public Realm getRealmByName(String realm) throws AccessDeniedException {
		Realm result = realmRepository.getRealmByName(realm);
		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.READ);
		return result;
	}

	@Override
	public Realm getRealmByHost(String host) {

//		List<DatabaseProperty> properties = realmRepository
//				.getPropertiesWithValue("realm.host", host);
//		if (properties.size() == 1) {
//			return realmRepository.getRealmById(properties.get(0).getResource()
//					.getId());
//		} else {
			return null;
//		}
	}

	@Override
	public Realm getRealmById(Long id) {
		return realmRepository.getRealmById(id);
	}

	@Override
	public Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceCreationException, AccessDeniedException {
		return getProviderForRealm(realm).createUser(realm, username,
				properties, principals);
	}

	@Override
	public Principal updateUser(Realm realm, Principal user, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceChangeException {
		return getProviderForRealm(realm).updateUser(realm, user, username,
				properties, principals);
	}

	@Override
	public boolean verifyPassword(Principal principal, char[] password) {
		return getProviderForRealm(principal.getRealm()).verifyPassword(
				principal, password);
	}

	@Override
	public Principal getPrincipalByName(Realm realm, String principalName,
			PrincipalType... type) {
		if (type.length == 0) {
			type = PrincipalType.ALL_TYPES;
		}
		return getProviderForRealm(realm).getPrincipalByName(principalName,
				realm, type);
	}

	@Override
	public void deleteRealm(String name) throws ResourceChangeException,
			ResourceNotFoundException, AccessDeniedException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.DELETE);

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
	public List<Realm> allRealms() {
		return filterRealms(null);
	}

	private List<Realm> filterRealms(Class<? extends RealmProvider> clz) {
		List<Realm> realms = realmRepository.allRealms();
		List<Realm> ret = new ArrayList<Realm>(realms);
		for (Realm r : realms) {
			if (!hasProviderForRealm(r)) {
				ret.remove(r);
			}
			if (clz != null
					&& !clz.isAssignableFrom(getProviderForRealm(r).getClass())) {
				ret.remove(r);
			}
			;
		}
		return ret;
	}

	@Override
	public List<Realm> allRealms(Class<? extends RealmProvider> clz) {
		return filterRealms(clz);
	}

	@Override
	public void changePassword(Principal principal, String oldPassword,
			String newPassword) throws ResourceCreationException,
			ResourceChangeException {

		RealmProvider provider = getProviderForRealm(principal.getRealm());
		if (provider.isReadOnly()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.realmIsReadOnly");
		}

		if (!verifyPassword(principal, oldPassword.toCharArray())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.invalidPassword");
		}
		provider.setPassword(principal, newPassword.toCharArray(), false);
	}

	@Override
	public void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {

		RealmProvider provider = getProviderForRealm(principal.getRealm());
		if (provider.isReadOnly()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.realmIsReadOnly");
		}

		provider.setPassword(principal, password.toCharArray(),
				forceChangeAtNextLogon);
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

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.CREATE);

		if (realmRepository.getRealmByName(name) != null) {
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE, "error.nameAlreadyExists", name);
			eventPublisher.publishEvent(new RealmCreatedEvent(this, ex,
					getCurrentSession(), name));
			throw ex;
		}

		try {
			Realm realm = realmRepository.createRealm(name, module, properties, getProviderForRealm(module));
			
			eventPublisher.publishEvent(new RealmCreatedEvent(this,
					getCurrentSession(), realm));

			return realm;
		} catch (Throwable e) {
			e.printStackTrace();
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE, "error.genericError", name, e.getMessage());
			eventPublisher.publishEvent(new RealmCreatedEvent(this, ex,
					getCurrentSession(), name));
			throw ex;
		}
	}

	@Override
	public Realm updateRealm(Realm realm, String name,
			Map<String, String> properties) throws AccessDeniedException,
			ResourceChangeException {

		if (!realm.getName().equals(name)) {
			if (realmRepository.getRealmByName(name) != null) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.nameAlreadyExists", name);
			}
		}

		try {
			
			String oldName = realm.getName();
			
			realm.setName(name);

			realm = realmRepository.saveRealm(realm, properties, getProviderForRealm(realm));

			eventPublisher.publishEvent(new RealmUpdatedEvent(this,
					getCurrentSession(), 
					oldName, 
					realmRepository.getRealmById(realm.getId())));
		} catch (Throwable ex) {
			ResourceChangeException e = new ResourceChangeException(
					RESOURCE_BUNDLE, "error.genericError", name,
					ex.getMessage());
			eventPublisher.publishEvent(new RealmUpdatedEvent(this, ex,
					getCurrentSession(), name));
			throw e;
		}
		return realm;
	}

	@Override
	public void deleteRealm(Realm realm) throws AccessDeniedException,
			ResourceChangeException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.DELETE);

		List<Realm> realms = realmRepository.allRealms();
		if (realms.size() == 1) {
			ResourceChangeException ex = new ResourceChangeException(RESOURCE_BUNDLE,
					"error.zeroRealms", realm.getName());
			eventPublisher.publishEvent(new RealmDeletedEvent(this, ex,
					getCurrentSession(), realm.getName()));
			throw ex;
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
				ResourceChangeException ex = new ResourceChangeException(RESOURCE_BUNDLE,
						"error.zeroSysAdmins", realm.getName());
				eventPublisher.publishEvent(new RealmDeletedEvent(this, ex,
						getCurrentSession(), realm.getName()));
				throw ex;
			}
		}

		String realmName = realm.getName();
		
		try {
			
			realmRepository.delete(realm);

			eventPublisher.publishEvent(new RealmDeletedEvent(this,
					getCurrentSession(), realmName));
			
		} catch (Throwable t) {
			eventPublisher.publishEvent(new RealmDeletedEvent(this, t,
					getCurrentSession(), realmName));
		}
	}

	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(Realm realm)
			throws AccessDeniedException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);
		
		return provider.getRealmProperties(realm);
		
	}
	
	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(String module)
			throws AccessDeniedException {
		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);
		
		return provider.getRealmProperties(null);
	}

	@Override
	public Principal getPrincipalById(Realm realm, Long id,
			PrincipalType... type) {
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
			List<Principal> principals) throws ResourceCreationException {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.createGroup(realm, name, principals);
	}

	@Override
	public Principal updateGroup(Realm realm, Principal group, String name,
			List<Principal> principals) throws ResourceChangeException {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.updateGroup(realm, group, name, principals);

	}

	@Override
	public void deleteGroup(Realm realm, Principal group)
			throws ResourceChangeException {

		RealmProvider provider = getProviderForRealm(realm);

		provider.deleteGroup(group);
	}

	@Override
	public void deleteUser(Realm realm, Principal user)
			throws ResourceChangeException {

		RealmProvider provider = getProviderForRealm(realm);

		provider.deleteUser(user);

	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(Principal principal)
			throws AccessDeniedException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.READ);
		
		RealmProvider provider = getProviderForRealm(principal.getRealm());
		
		return provider.getUserProperties(principal);
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(String module)
			throws AccessDeniedException {

		assertPermission(PermissionStrategy.REQUIRE_ANY, RealmPermission.READ);
		
		RealmProvider provider = getProviderForRealm(module);
		
		return provider.getUserProperties(null);
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

//	@Override
//	public List<ResourceProperty> getRealmProperties(Realm realm) {
//
//		List<ResourceProperty> properties = new ArrayList<ResourceProperty>();
//		properties.addAll(realmRepository.getPropertiesForResource(realm));
//		return properties;
//	}
//
//	@Override
//	public List<ResourceProperty> getUserProperties(Principal principal) {
//		List<ResourceProperty> properties = new ArrayList<ResourceProperty>();
//		properties.addAll(realmRepository.getgetPropertiesForResource(principal));
//		return properties;
//	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal,
			PrincipalType type) {
		return getProviderForRealm(principal.getRealm())
				.getAssociatedPrincipals(principal, type);
	}

	@Override
	public List<?> getPrincipals(Realm realm, PrincipalType type,  String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		return getProviderForRealm(realm).getPrincipals(realm, type, searchPattern, start,
				length, sorting);
	}

	@Override
	public Long getPrincipalCount(Realm realm, PrincipalType type) {
		return getProviderForRealm(realm).getPrincipalCount(realm, type);
	}

	@Override
	public boolean findUniquePrincipal(String user) {
		
		int found = 0;
		for(Realm r : allRealms()) {
			Principal p = getPrincipalByName(r, user, PrincipalType.USER);
			if(p!=null) {
				found++;
			}
		}
		return found==1;
	}

	@Override
	public Principal getUniquePrincipal(String username) throws ResourceNotFoundException {
		int found = 0;
		Principal ret = null;
		for(Realm r : allRealms()) {
			Principal p = getPrincipalByName(r, username, PrincipalType.USER);
			if(p!=null) {
				ret = p;
				found++;
			}
		}
		if(found != 1) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "principal.notFound");
		}
		return ret;
	}
	
	

}
