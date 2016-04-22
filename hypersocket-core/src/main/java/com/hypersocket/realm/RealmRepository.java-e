/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.List;
import java.util.Map;

import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.tables.ColumnSort;

public interface RealmRepository extends
		AbstractResourceRepository<RealmResource> {

	public List<Realm> allRealms();

	public Realm getRealmById(Long id);

	public Realm getRealmByName(String name);

	public Realm getRealmByHost(String host);

	public void delete(Realm realm);

	Realm saveRealm(Realm realm, Map<String, String> properties,
			RealmProvider provider);

	Realm createRealm(String name, String uuid, String module,
			Map<String, String> properties, RealmProvider provider);

	Realm getRealmByName(String name, boolean deleted);

	List<Realm> allRealms(String resourceKey);

	List<Realm> searchRealms(String searchPattern, int start, int length,
			ColumnSort[] sorting);

	public Long countRealms(String searchPattern);

	Realm saveRealm(Realm realm);

	public Realm getDefaultRealm();

	public Realm setDefaultRealm(Realm realm);

}
