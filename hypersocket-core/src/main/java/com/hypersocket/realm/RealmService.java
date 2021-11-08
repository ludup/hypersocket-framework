/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.hypersocket.auth.PasswordEnabledAuthenticatedService;
import com.hypersocket.export.CommonEndOfLine;
import com.hypersocket.export.CommonEndOfLineEnum;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.TableFilter;

public interface RealmService extends PasswordEnabledAuthenticatedService {

	static final String RESOURCE_BUNDLE = "RealmService";
	//static final String SYSTEM_REALM = "System";
	static final String SYSTEM_PRINCIPAL = "system";
	static final String MODULE = "realms";

	public final static String KNOWN_HOSTS_ATTR = "realm.knownHosts";

	void registerRealmProvider(RealmProvider provider);

	Realm createPrimaryRealm(String name, String module, Map<String, String> properties)
			throws AccessDeniedException, ResourceException;
	
	Realm createRealm(String name, String module, Realm parent, Long owner, Map<String, String> properties)
			throws AccessDeniedException, ResourceException;

	void deleteRealm(String name) throws ResourceException, ResourceNotFoundException, AccessDeniedException;

	List<Realm> allRealms() throws AccessDeniedException;

	Realm getRealmByName(String realm);

	Realm getRealmByHost(String host);

	Realm getRealmById(Long id);

	Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
						 String password, boolean forceChange, boolean selfCreated, boolean sendNotifications)
			throws ResourceException, AccessDeniedException;
	
	Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			 PasswordCreator password, boolean forceChange, boolean selfCreated, boolean sendNotifications)
					 throws ResourceException, AccessDeniedException;

	Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated, Principal parent,
			RealmProvider provider, boolean sendNotifications, PrincipalType type) throws ResourceException, AccessDeniedException;

	default Principal createUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated, Principal parent,
			RealmProvider provider, boolean sendNotifications) throws ResourceException, AccessDeniedException {
		return createUser(realm, username, properties, principals, passwordCreator, forceChange, selfCreated, parent, provider, sendNotifications, PrincipalType.USER);
	}
	
	Principal updateUser(Realm realm, Principal user, String username, Map<String, String> properties,
						 List<Principal> principals) throws ResourceException, AccessDeniedException;

	Principal getPrincipalByName(Realm realm, String principalName, PrincipalType... type);

	boolean verifyPassword(Principal principal, char[] password) throws LogonException, IOException;

	void setPassword(Principal principal, String password, boolean forceChangeAtNextLogon, boolean administrative)
			throws AccessDeniedException, ResourceException;
	
	void setPassword(Principal principal, String password, boolean forceChangeAtNextLogon, boolean resendNewUserNotification, 
			boolean administrative) throws AccessDeniedException, ResourceException;

	void changePassword(Principal principal, String oldPassword, String newPassword)
			throws AccessDeniedException, ResourceException;

	Principal getSystemPrincipal();

	List<RealmProvider> getProviders() throws AccessDeniedException;

	void deleteRealm(Realm realm) throws AccessDeniedException, ResourceException;

	Collection<PropertyCategory> getRealmPropertyTemplates(Realm realm) throws AccessDeniedException;

	Realm updateRealm(Realm realm, String name, String module, Map<String, String> properties)
			throws AccessDeniedException, ResourceException;

	Principal getPrincipalById(Realm realm, Long id, PrincipalType... type) throws AccessDeniedException;

	boolean requiresPasswordChange(Principal principal, Realm realm);

	Principal createGroup(Realm realm, String name, Map<String, String> properties, List<Principal> principals,
						  List<Principal> groups) throws ResourceException, AccessDeniedException;

	Principal updateGroup(Realm realm, Principal principal, String name, Map<String, String> properties,
						  List<Principal> principals, List<Principal> groups) throws AccessDeniedException, ResourceException;

	void deleteGroup(Realm realm, Principal group, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException;

	void deleteUser(Realm realm, Principal user, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getUserPropertyTemplates(Principal principalById) throws AccessDeniedException;

	Collection<PropertyCategory> getUserPropertyTemplates() throws AccessDeniedException;

	Collection<PropertyCategory> getUserPropertyTemplates(String module) throws AccessDeniedException;

	List<Principal> getAssociatedPrincipals(Principal principal);

	List<Principal> getAssociatedPrincipals(Principal principal, PrincipalType type);

	List<Realm> allRealms(Class<? extends RealmProvider> clz);

	Collection<PropertyCategory> getRealmPropertyTemplates(String module) throws AccessDeniedException;

	boolean findUniquePrincipal(String user);

	Iterator<Principal> iterateUsers(Realm realm);

	Iterator<Principal> iterateGroups(Realm realm) throws AccessDeniedException;

	Set<Principal> getUsers(Realm realm, int max);

	Set<Principal> getGroups(Realm realm, int max) throws AccessDeniedException;

	List<?> getRealms(String searchPattern, String searchColumn, int start, int length, ColumnSort[] sorting) throws AccessDeniedException;

	Long getRealmCount(String searchPattern, String searchColumn) throws AccessDeniedException;

	Collection<PropertyCategory> getGroupPropertyTemplates(String module) throws AccessDeniedException;

	void updateProfile(Realm currentRealm, Principal principal, Map<String, String> values)
			throws AccessDeniedException, ResourceException;

	RealmProvider getProviderForRealm(Realm realm);

	RealmProvider getProviderForRealm(String module);

	String getRealmProperty(Realm realm, String resourceKey);

	int getRealmPropertyInt(Realm realm, String resourceKey);

	String getPrincipalAddress(Principal principal, MediaType type) throws MediaNotFoundException;

	String getPrincipalDescription(Principal principal);

	boolean isReadOnly(Realm realm);

	boolean supportsAccountUnlock(Realm realm) throws ResourceException;

	boolean supportsAccountDisable(Realm realm) throws ResourceException;

	Principal disableAccount(Principal principal) throws AccessDeniedException, ResourceException;

	Principal enableAccount(Principal principal) throws ResourceException, AccessDeniedException;

	Principal unlockAccount(Principal principal) throws ResourceException, AccessDeniedException;

	Realm getSystemRealm();

	void registerRealmListener(RealmListener listener);

	Realm getDefaultRealm();

	List<Realm> allRealms(boolean ignoreMissingProvider);

	Realm setDefaultRealm(Realm realm) throws AccessDeniedException;

	String getRealmHostname(Realm realm);

	Collection<String> getUserPropertyNames(Realm realm, Principal principal) throws AccessDeniedException;

	Collection<String> getEditablePropertyNames(Realm realm) throws AccessDeniedException;
	
	Collection<String> getVisiblePropertyNames(Realm realm) throws AccessDeniedException;

	String[] getRealmPropertyArray(Realm realm, String resourceKey);

	Collection<PropertyCategory> getUserProfileTemplates(Principal principal) throws AccessDeniedException;

	Collection<String> getUserPropertyNames(String id) throws AccessDeniedException;

	boolean isRealmStrictedToHost(Realm realm);

	Collection<String> getUserVariableNames(Realm realm, Principal principal);

	void unregisterRealmProvider(RealmProvider provider);

	boolean isRegistered(RealmProvider provider);

	boolean verifyPrincipal(String principal, Realm realm);

	String getPrincipalEmail(Principal currentPrincipal);

	String getPrincipalPhone(Principal principal);

	Map<String, String> getUserPropertyValues(Principal principal, String... variableNames);

	void setRealmProperty(Realm realm, String resourceKey, String value) throws AccessDeniedException;

	Principal getUniquePrincipal(String username, PrincipalType... type) throws ResourceNotFoundException;

	Realm getRealmByHost(String host, Realm defaultRealm);

	long getPrincipalCount(Realm realm, PrincipalType type);
	
	long getPrincipalCount(Collection<Realm> realms, PrincipalType type);

	boolean getRealmPropertyBoolean(Realm realm, String string);

	void registerPrincipalProcessor(PrincipalProcessor processor);

	String getDecryptedValue(Realm realm, String resourceKey);

	Collection<PropertyCategory> getGroupPropertyTemplates(Principal principal) throws AccessDeniedException;

	boolean canChangePassword(Principal currentPrincipal);

	Principal updateUserProperties(Principal user, Map<String, String> properties)
			throws ResourceException, AccessDeniedException;

	Realm getRealmByOwner(Long owner) throws AccessDeniedException;

	List<?> searchPrincipals(Realm realm, PrincipalType type, String module, String searchColumn, String searchPattern, int start,
							 int length, ColumnSort[] sorting) throws AccessDeniedException;

	Long getSearchPrincipalsCount(Realm realm, PrincipalType type, String module, String searchColumn, String searchPattern)
			throws AccessDeniedException;

	Principal createLocalUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
							  String password, boolean forceChange, boolean selfCreated, boolean sendNotifications)
			throws ResourceException, AccessDeniedException;

	Long getSearchPrincipalsCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern) throws AccessDeniedException;

	List<?> searchPrincipals(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start, int length,
							 ColumnSort[] sorting) throws AccessDeniedException;

	UserPrincipal<?> getPrincipalByFullName(Realm realm, String value) throws AccessDeniedException, ResourceNotFoundException;

	Principal getPrincipalByEmail(Realm realm, String email) throws AccessDeniedException, ResourceNotFoundException;

	Principal getDeletedPrincipalById(Realm realm, Long id, PrincipalType... type) throws AccessDeniedException;

	Collection<PropertyCategory> getUserProperties(Principal principal);

	Map<String, String> getUserPropertyValues(Principal principal);

	String getProfileProperty(Principal principal, String string);

	List<Principal> getGroupGroups(Principal principal);

	List<Principal> getGroupUsers(Principal principal);

	List<Principal> getUserGroups(Principal principal);

	Map<String, String> filterSecretProperties(Principal principal, RealmProvider provider,
											   Map<String, String> properties);

	Principal getPrincipalById(Long id);

	Long getUserPropertyLong(Principal principal, String resourceKey);

	Integer getUserPropertyInt(Principal principal, String resourceKey);

	boolean getUserPropertyBoolean(Principal principal, String resourceKey);

	String getUserProperty(Principal principal, String resourceKey);
	
	void setUserPropertyLong(Principal principal, String resourceKey, Long val);

	void setUserPropertyInt(Principal principal, String resourceKey, Integer val);

	void setUserPropertyBoolean(Principal principal, String resourceKey, Boolean val);

	void setUserProperty(Principal principal, String resourceKey, String val);

	void deleteRealms(List<Realm> realms) throws ResourceException, AccessDeniedException;
	
	List<Realm> getRealmsByIds(Long...ids) throws AccessDeniedException;
	
	void deleteUsers(Realm realm, List<Principal> users, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException;
	
	List<Principal> getUsersByIds(Long...ids) throws AccessDeniedException;
	
	void deleteGroups(Realm realm, List<Principal> groups, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException;
	
	List<Principal> getGroupsByIds(Long...ids) throws AccessDeniedException;

	Realm getRealmByNameAndOwner(String name, Realm realm);

	void assignUserToGroup(Principal user, Principal group) throws ResourceException, AccessDeniedException;

	void unassignUserFromGroup(Principal user, Principal group) throws ResourceException, AccessDeniedException;

	boolean isLocked(Principal principal) throws ResourceException;

	Realm resetRealm(Realm realm) throws ResourceException, AccessDeniedException;

	Principal getUniquePrincipalForRealm(String username, Realm realm, PrincipalType... type) throws ResourceNotFoundException;

	boolean isUserSelectingRealm();

	String[] getRealmHostnames(Realm realm);

	void registerPrincipalFilter(TableFilter filter);

	Collection<TableFilter> getPrincipalFilters();

	Collection<Realm> getRealmsByOwner();

	void registerOwnershipResolver(RealmOwnershipResolver resolver);

	Collection<Realm> getRealmsByParent(Realm currentRealm);

	Map<String, String> getRealmProperties(Realm realm);

	List<CommonEndOfLine> getCommonEndOfLine();

	void downloadCSV(Realm realm, String search, String searchPattern, String module, String filename,
			boolean outputHeaders, String delimiters, CommonEndOfLineEnum eol, String wrap, String escape,
			String attributes, ColumnSort[] sort, OutputStream output, Locale locale) throws AccessDeniedException, UnsupportedEncodingException;

	Collection<String> getAllUserAttributeNames(Realm realm);

	boolean isDisabled(Principal principal);

	Principal createLocalUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated, boolean sendNotifications, PrincipalType type)
			throws ResourceException, AccessDeniedException;

	default Principal createLocalUser(Realm realm, String username, Map<String, String> properties, List<Principal> principals,
			PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated, boolean sendNotifications)
			throws ResourceException, AccessDeniedException {
		return createLocalUser(realm, username, properties, principals, passwordCreator, forceChange, selfCreated, sendNotifications, PrincipalType.USER);
	}

	void undeleteUser(Realm realm, Principal user) throws ResourceException, AccessDeniedException;

	void undeleteUsers(Realm realm, List<Principal> users) throws ResourceException, AccessDeniedException;

	Collection<Realm> getPublicRealmsByParent(Realm currentRealm);

	Principal getPrincipalByUUID(String uuid);

	Long getRealmCount();

	boolean isChangingPassword(Principal principal);

	void assertChangeCredentials(Principal principal) throws AccessDeniedException, ResourceException;

	RealmProvider getLocalProvider();

}

