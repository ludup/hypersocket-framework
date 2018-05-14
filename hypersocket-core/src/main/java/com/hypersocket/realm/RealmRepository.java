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

	public List<Realm> allRealms();

	public Realm getRealmById(Long id);

	public Realm getRealmByName(String name);

	public Realm getRealmByHost(String host);

	public void delete(Realm realm);

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
							 ColumnSort[] sorting);

	public Long countRealms(String searchPattern, String searchColumn);

	Realm saveRealm(Realm realm);

	public Realm getDefaultRealm();

	public Realm setDefaultRealm(Realm realm);

	Realm getRealmByOwner(Long owner);

	public Realm getSystemRealm();
	
	List<Realm> getRealmsByIds(Long...ids);

	Realm getRealmByNameAndOwner(String name, Realm owner);

	Collection<Realm> getRealmsByParent(Realm currentRealm);

	void deleteRealmSoftly(Realm realm);

}
