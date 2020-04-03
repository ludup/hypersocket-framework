/*******************************************************************************
x * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.repository.AbstractRepository;
import com.hypersocket.tables.ColumnSort;


public interface LocalUserRepository extends ResourceTemplateRepository, AbstractRepository<Long> {

	LocalUser createUser(String username, Realm realm);
	
	LocalGroup createGroup(String name, Realm realm);
	
	LocalUser getUserByName(String name, Realm realm);
	
	LocalGroup getGroupByName(String name, Realm realm);
	
	void assign(LocalUser user, LocalGroup group);
	
	void unassign(LocalUser user, LocalGroup group);

	Collection<? extends Principal> allUsers(Realm realm);

	Collection<? extends Principal> allGroups(Realm realm);
	
	Iterator<LocalUser> iterateUsers(Realm realm, ColumnSort[] sorting);
	
	Iterator<LocalGroup> iterateGroups(Realm realm, ColumnSort[] sorting);

	LocalUserCredentials getCredentials(LocalUser user);
	
	void saveGroup(LocalGroup group);
	
	void saveUser(LocalUser user, Map<String,String> properties);

	void saveCredentials(LocalUserCredentials creds);

	Principal getUserByNameAndType(String principalName, Realm realm,
			PrincipalType system);

	Principal getUserById(Long id, Realm realm, boolean deleted);

	Principal getGroupById(Long id, Realm realm, boolean deleted);

	Principal getUserByIdAndType(Long id, Realm realm,
			PrincipalType system);

	void deleteGroup(LocalGroup group);

	void deleteUser(LocalUser usr);

	Long countUsers(Realm realm, String searchColumn, String searchPattern);

	Long countGroups(Realm realm, String searchColumn, String searchPattern);

	List<?> getGroups(Realm realm, String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting);

	List<?> getUsers(Realm realm, String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting);

	Collection<? extends Principal> getGroupsByUser(LocalUser principal);

	Collection<? extends Principal> getUsersByGroup(LocalGroup principal);

	Collection<? extends Principal> getGroupsByGroup(LocalGroup principal);

	Principal getUserByEmail(String email, Realm realm);

	UserPrincipal<?> getUserByFullName(String fullName, Realm realm);

	void resetRealm(Iterator<Principal> admins);

	void deleteRealm(Realm realm);

	Collection<LocalUserCredentials> allCredentials();
}
