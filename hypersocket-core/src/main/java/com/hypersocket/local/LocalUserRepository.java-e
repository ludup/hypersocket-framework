/*******************************************************************************
x * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepository;
import com.hypersocket.tables.ColumnSort;


public interface LocalUserRepository extends ResourceTemplateRepository, AbstractRepository<Long> {

	public LocalUser createUser(String username, Realm realm);
	
	public LocalGroup createGroup(String name, Realm realm);
	
	public LocalUser getUserByName(String name, Realm realm);
	
	public LocalGroup getGroupByName(String name, Realm realm);
	
	public void assign(LocalUser user, LocalGroup group);
	
	public void unassign(LocalUser user, LocalGroup group);

	public Collection<? extends Principal> allUsers(Realm realm);

	public Collection<? extends Principal> allGroups(Realm realm);

	public LocalUserCredentials getCredentials(LocalUser user);
	
	public void saveGroup(LocalGroup group);
	
	public void saveUser(LocalUser user, Map<String,String> properties);

	public void saveCredentials(LocalUserCredentials creds);

	public Principal getUserByNameAndType(String principalName, Realm realm,
			PrincipalType system);

	public Principal getUserById(Long id, Realm realm);

	public Principal getGroupById(Long id, Realm realm);

	public Principal getUserByIdAndType(Long id, Realm realm,
			PrincipalType system);

	public void deleteGroup(LocalGroup group);

	public void deleteUser(LocalUser usr);

	Long countUsers(Realm realm, String searchPattern);

	Long countGroups(Realm realm, String searchPattern);

	List<?> getGroups(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting);

	List<?> getUsers(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting);

	public Collection<? extends Principal> getGroupsByUser(LocalUser principal);

	public Collection<? extends Principal> getUsersByGroup(LocalGroup principal);

	public Collection<? extends Principal> getGroupsByGroup(LocalGroup principal);
}
