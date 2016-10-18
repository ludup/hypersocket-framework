/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceConfirmationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tables.ColumnSort;

public interface RealmProvider extends ResourceTemplateRepository {

	void testConnection(Map<String, String> properties) throws IOException, ResourceConfirmationException;

	void testConnection(Map<String, String> properties, Realm realm) throws IOException, ResourceConfirmationException;

	List<Principal> allPrincipals(Realm realm, PrincipalType... types);

	Principal getPrincipalByName(String principalName, Realm realm, PrincipalType... acceptTypes);

	boolean verifyPassword(Principal principal, char[] password);

	boolean isReadOnly(Realm realm);

	boolean supportsAccountUnlock(Realm realm) throws IOException;

	Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			String password, boolean forceChange) throws ResourceException;

	Principal updateUser(Realm realm, Principal user, String username, Map<String, String> properties,
			List<Principal> principals) throws ResourceException;

	void setPassword(Principal principal, String password, boolean forceChangeAtNextLogon, boolean administrative)
			throws ResourceException;

	void setPassword(Principal principal, char[] password, boolean forceChangeAtNextLogon, boolean administrative)
			throws ResourceException;

	boolean requiresPasswordChange(Principal principal);

	Principal getPrincipalById(Long id, Realm realm, PrincipalType[] type);

	Principal getDeletedPrincipalById(Long id, Realm realm, PrincipalType[] type);
	
	Principal createGroup(Realm realm, String name, Map<String, String> properties, List<Principal> principals,
			List<Principal> groups) throws ResourceException;

	Principal createGroup(Realm realm, String name, Map<String, String> properties) throws ResourceException;

	void deleteGroup(Principal group) throws ResourceException;

	Principal updateGroup(Realm realm, Principal group, String name, Map<String, String> properties,
			List<Principal> principals, List<Principal> groups) throws ResourceException;

	void deleteUser(Principal user) throws ResourceException;

	void deleteRealm(Realm realm) throws ResourceException;

	List<Principal> getAssociatedPrincipals(Principal principal);

	String getModule();

	String getResourceBundle();
	
	boolean canCreate();

	List<Principal> getAssociatedPrincipals(Principal principal, PrincipalType type);

	Long getPrincipalCount(Realm realm, PrincipalType type, String searchPattern);

	List<?> getPrincipals(Realm realm, PrincipalType type, String searchPattern, int start, int length,
			ColumnSort[] sorting);

	Collection<PropertyCategory> getUserProperties(Principal principal);

	Collection<PropertyCategory> getRealmProperties(Realm realm);

	Collection<PropertyCategory> getGroupProperties(Principal principal);

	String getAddress(Principal principal, MediaType type) throws MediaNotFoundException;

	String getPrincipalDescription(Principal principal);

	boolean supportsAccountDisable(Realm realm) throws IOException;

	Principal disableAccount(Principal principal) throws ResourceException;

	Principal enableAccount(Principal principal) throws ResourceException;

	Principal unlockAccount(Principal principal) throws ResourceException;

	Set<String> getUserPropertyNames(Principal principal);

	String getUserPropertyValue(Principal principal, String name);

	Set<String> getGroupPropertyNames(Principal principal);

	void changePassword(Principal principal, char[] oldPassword, char[] newPassword)
			throws ResourceChangeException, ResourceException;

	Set<String> getUserVariableNames(Principal principal);

	Map<String, String> getUserPropertyValues(Principal principal);

	Set<String> getDefaultUserPropertyNames();

	boolean hasPropertyValueSet(Principal principal, String string);

	String getDecryptedValue(Realm realm, String resourceKey);

	boolean isDisabled(Principal principal);

	boolean canChangePassword(Principal principal);

	Principal updateUserProperties(Principal principal, Map<String, String> properties) throws ResourceException;

	Principal getPrincipalByEmail(Realm realm, String email);

	List<Principal> getUserGroups(Principal principal);

	List<Principal> getGroupUsers(Principal principal);

	List<Principal> getGroupGroups(Principal principal);
}
