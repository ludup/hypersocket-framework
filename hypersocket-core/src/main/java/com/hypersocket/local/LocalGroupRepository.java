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

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepository;
import com.hypersocket.tables.ColumnSort;


public interface LocalGroupRepository extends ResourceTemplateRepository, AbstractRepository<Long> {

	LocalGroup createGroup(String name, Realm realm);
	
	LocalGroup getGroupByName(String name, Realm realm);
	
	Collection<? extends Principal> allGroups(Realm realm);
	
	Iterator<LocalGroup> iterateGroups(Realm realm, ColumnSort[] sorting);

	void saveGroup(LocalGroup group);

	Principal getGroupById(Long id, Realm realm, boolean deleted);

	void deleteGroup(LocalGroup group);

	Long countGroups(Realm realm, String searchColumn, String searchPattern);

	List<?> getGroups(Realm realm, String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting);

	Collection<? extends Principal> getGroupsByGroup(LocalGroup principal);

	void resetRealm(Iterator<Principal> admins);

	void deleteRealm(Realm realm);
	
	int getNextPosixId(Realm realm);
}
