/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface RealmProvider extends ResourceTemplateRepository {
	
	public interface TestConnectionRetry {
		void retry(Map<String, String> updatedProperties) throws Exception;
	}

	void testConnection(Map<String, String> properties, TestConnectionRetry retry) throws IOException, ResourceException;

	void testConnection(Map<String, String> properties, Realm realm, TestConnectionRetry retry) throws ResourceException;

	void assertCreateRealm(Map<String, String> properties) throws ResourceException;
	
	Iterator<Principal> iterateAllPrincipals(Realm realm, PrincipalType... types);

	Principal getPrincipalByName(String principalName, Realm realm, PrincipalType... acceptTypes);

	boolean verifyPassword(Principal principal, char[] password) throws LogonException, IOException;

	boolean isReadOnly(Realm realm);

	boolean supportsAccountUnlock(Realm realm) throws ResourceException;
	
	Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			PasswordCreator passwordCreator, boolean forceChange, PrincipalType type) throws ResourceException;

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

	void deleteGroup(Principal group, boolean deleteLocallyOnly) throws ResourceException;

	Principal updateGroup(Realm realm, Principal group, String name, Map<String, String> properties,
			List<Principal> principals, List<Principal> groups) throws ResourceException;

	void deleteUser(Principal user, boolean deleteLocallyOnly) throws ResourceException;

	void deleteRealm(Realm realm) throws ResourceException;

	List<Principal> getAssociatedPrincipals(Principal principal);

	String getModule();

	String getResourceBundle();

	List<Principal> getAssociatedPrincipals(Principal principal, PrincipalType type);

	Long getPrincipalCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern);

	List<?> getPrincipals(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting);

	Collection<PropertyCategory> getUserProperties(RealmResource principal);

	Collection<PropertyCategory> getRealmProperties(Realm realm);

	Collection<PropertyCategory> getGroupProperties(Principal principal);

	String getAddress(Principal principal, MediaType type) throws MediaNotFoundException;

	String getPrincipalDescription(Principal principal);

	boolean supportsAccountDisable(Realm realm) throws ResourceException;
	
	boolean supportsThumbnailPhoto(Realm realm) throws ResourceException;

	Principal disableAccount(Principal principal) throws ResourceException;

	Principal enableAccount(Principal principal) throws ResourceException;

	Principal unlockAccount(Principal principal) throws ResourceException;

	Set<String> getUserPropertyNames(Principal principal);

	String getUserPropertyValue(Principal principal, String name);

	Set<String> getGroupPropertyNames(Principal principal);

	void changePassword(Principal principal, char[] oldPassword, char[] newPassword, boolean checkCurrent)
			throws ResourceException, ResourceException;

	Set<String> getUserVariableNames(Principal principal);

	Map<String, String> getUserPropertyValues(Principal principal);

	Set<String> getDefaultUserPropertyNames();

	boolean hasPropertyValueSet(Principal principal, String string);

	String getDecryptedValue(Realm realm, String resourceKey);

	boolean isDisabled(Principal principal);

	boolean canChangePassword(Principal principal);

	Principal updateUserProperties(Principal principal, Map<String, String> properties) throws ResourceException;

	Long getUserPropertyLong(Principal principal, String resourceKey);
	
	Integer getUserPropertyInt(Principal principal, String resourceKey);
	
	boolean getUserPropertyBoolean(Principal principal, String resourceKey);
	
	String getUserProperty(Principal principal, String resourceKey);
	
	Principal getPrincipalByEmail(Realm realm, String email);

	List<Principal> getUserGroups(Principal principal);

	List<Principal> getGroupUsers(Principal principal);

	List<Principal> getGroupGroups(Principal principal);

	void setUserProperty(Principal principal, String resourceKey, Long val);

	void setUserProperty(Principal principal, String resourceKey, Integer val);

	void setUserProperty(Principal principal, String resourceKey, Boolean val);

	void setUserProperty(Principal principal, String resourceKey, String val);

	Principal reconcileUser(Principal principal) throws ResourceException;

	void resetRealm(Realm realm) throws ResourceNotFoundException, AccessDeniedException;

	void verifyConnection(Realm realm) throws ResourceException;

	Collection<String> getEditablePropertyNames(Realm realm);

	Collection<String> getVisiblePropertyNames(Realm realm);

	Collection<PropertyCategory> getPrincipalTemplate(Resource resource);

	Collection<PropertyCategory> getPrincipalTemplate();

	Map<String, String> getPrincipalTemplateProperties(Resource resource);

	boolean isEnabled();

	UserPrincipal<?> getPrincipalByFullName(Realm realm, String fullName);

}
