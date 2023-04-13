/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.resource.AbstractSimpleResourceRepository;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tables.ColumnSort;

public interface RealmRepository extends
		AbstractSimpleResourceRepository<Realm> {

	List<Realm> allRealms();

	Realm getRealmById(Long id);

	Realm getRealmByName(String name);

	Realm getRealmByHost(String host);

	void delete(Realm realm);

	Realm saveRealm(Realm realm, Map<String, String> properties,
					RealmProvider provider,
					  @SuppressWarnings("unchecked") TransactionOperation<Realm> ... ops) throws ResourceException;

	Realm createRealm(String name, String uuid, String module,
					  Map<String, String> properties, RealmProvider provider,
					  Realm parent, Long owner, Boolean publicRealm,
					  @SuppressWarnings("unchecked") TransactionOperation<Realm> ... ops) throws ResourceException;

	Realm getRealmByName(String name, boolean deleted);

	List<Realm> allRealms(String resourceKey);

	List<Realm> searchRealms(String searchPattern, String searchColumn, int start, int length,
							 ColumnSort[] sorting, Realm currentRealm, Collection<Realm> filter);

	Long countRealms(String searchPattern, String searchColumn, Realm currentRealm, Collection<Realm> filter);

	Realm saveRealm(Realm realm);

	Realm getDefaultRealm();

	Realm setDefaultRealm(Realm realm);

	Realm getRealmByOwner(Long owner);

	Realm getSystemRealm();
	
	Realm getFakeRealm();
	
	List<Realm> getRealmsByIds(Long...ids);

	Realm getRealmByNameAndOwner(String name, Realm owner);

	Collection<Realm> getRealmsByParent(Realm currentRealm);

	void deleteRealmSoftly(Realm realm);

	void deleteRealmRoles(Realm realm);

	long countPrimaryRealms();

	Collection<Realm> getPublicRealmsByParent(Realm realm);

}
