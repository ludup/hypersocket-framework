/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface RealmService extends AuthenticatedService {

	static final String RESOURCE_BUNDLE = "RealmService";
	static final String SYSTEM_REALM = "System";
	static final String SYSTEM_PRINCIPAL = "system";
	static final String MODULE = "realms";

	void registerRealmProvider(RealmProvider provider);

	Realm createRealm(String name, String module, Map<String, String> properties)
			throws AccessDeniedException, ResourceCreationException;

	void deleteRealm(String name) throws ResourceChangeException,
			ResourceNotFoundException, AccessDeniedException;

	List<Realm> allRealms();

	Realm getRealmByName(String realm) throws AccessDeniedException;

	Realm getRealmByHost(String host);

	Realm getRealmById(Long id);

	Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceCreationException, AccessDeniedException;

	Principal updateUser(Realm realm, Principal user, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceChangeException, AccessDeniedException;

	List<Principal> allPrincipals(Realm realm, PrincipalType... type);

	Principal getPrincipalByName(Realm realm, String principalName,
			PrincipalType... type);

	boolean verifyPassword(Principal principal, char[] password);

	void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException;

	void changePassword(Principal principal, String oldPassword,
			String newPassword) throws ResourceCreationException,
			ResourceChangeException;

	Principal getSystemPrincipal();

	List<RealmProvider> getProviders() throws AccessDeniedException;

	void deleteRealm(Realm realm) throws AccessDeniedException,
			ResourceChangeException;

	Collection<PropertyCategory> getRealmPropertyTemplates(Realm realm)
			throws AccessDeniedException;

	Realm updateRealm(Realm realm, String name, Map<String, String> properties)
			throws AccessDeniedException, ResourceChangeException;

	Principal getPrincipalById(Realm realm, Long id, PrincipalType... type);

	boolean requiresPasswordChange(Principal principal, Realm realm);

	Principal createGroup(Realm realm, String name, List<Principal> principals)
			throws ResourceCreationException;

	Principal updateGroup(Realm realm, Principal principal, String name,
			List<Principal> principals) throws ResourceChangeException;

	void deleteGroup(Realm realm, Principal group)
			throws ResourceChangeException;

	void deleteUser(Realm realm, Principal user) throws ResourceChangeException;

	Collection<PropertyCategory> getUserPropertyTemplates(Principal principalById)
			throws AccessDeniedException;

	Collection<PropertyCategory> getUserPropertyTemplates(String module)
			throws AccessDeniedException;
	
	List<Principal> getAssociatedPrincipals(Principal principal);

	List<Principal> getAssociatedPrincipals(Principal principal,
			PrincipalType type);

	List<Realm> allRealms(Class<? extends RealmProvider> clz);

	RealmProvider getProviderForRealm(Realm realm);

	List<?> getPrincipals(Realm realm, PrincipalType type, String searchPattern, int start,
			int length, ColumnSort[] sorting);

	Long getPrincipalCount(Realm realm, PrincipalType type);

	Collection<PropertyCategory> getRealmPropertyTemplates(String module)
			throws AccessDeniedException;

	boolean findUniquePrincipal(String user);

	Principal getUniquePrincipal(String username) throws ResourceNotFoundException;

}
