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

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.HiddenFilter;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.tables.ColumnSort;

@Repository
public class RealmRepositoryImpl extends
		AbstractResourceRepositoryImpl<RealmResource> implements
		RealmRepository {


	@Override
	@Transactional
	public Realm createRealm(String name, String uuid, String module,
			Map<String, String> properties, RealmProvider provider) {
		Realm realm = new Realm();
		realm.setName(name);
		realm.setResourceCategory(module);
		realm.setUuid(uuid);

		save(realm);

		provider.setValues(realm, properties);

		return realm;
	}

	@Override
	@Transactional
	public Realm saveRealm(Realm realm) {
		save(realm);
		flush();
		refresh(realm);
		return realm;
	}

	@Override
	@Transactional
	public Realm saveRealm(Realm realm, Map<String, String> properties,
			RealmProvider provider) {

		boolean isNew = realm.getId() == null;
		save(realm);

		provider.setValues(realm, properties);

		if (!isNew) {
			refresh(realm);
		}

		return realm;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Realm> allRealms() {
		return allEntities(Realm.class, new HiddenFilter(),
				new DeletedCriteria(false), new DistinctRootEntity());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Realm> searchRealms(String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		return search(Realm.class, "name", searchPattern, start, length,
				sorting, new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("hidden", false));
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<Realm> allRealms(String resourceKey) {
		return list("resourceCategory", resourceKey, Realm.class,
				new HiddenFilter(), new DeletedCriteria(false),
				new DistinctRootEntity());
	}

	protected Realm getRealm(String column, Object value) {
		return get(column, value, Realm.class, true);
	}

	@Override
	@Transactional(readOnly = true)
	public Realm getRealmById(Long id) {
		return get("id", id, Realm.class);
	}

	@Override
	@Transactional(readOnly = true)
	public Realm getRealmByName(String name) {
		return getRealm("name", name);
	}

	@Override
	@Transactional(readOnly = true)
	public Realm getRealmByName(String name, boolean deleted) {
		return get("name", name, Realm.class, new DeletedCriteria(deleted));
	}

	@Override
	@Transactional(readOnly = true)
	public Realm getRealmByHost(String host) {
		return getRealm("host", host);
	}

	@Override
	@Transactional
	public void delete(Realm realm) {
		realm.setDeleted(true);
		realm.setName(realm.getName() + "[#" + realm.getId() + " deleted]");

		save(realm);
	}

	@Override
	@Transactional(readOnly = true)
	public Long countRealms(String searchPattern) {
		return getCount(Realm.class, "name", searchPattern,
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("hidden", false));
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}

	@Override
	protected Class<RealmResource> getResourceClass() {
		return RealmResource.class;
	}

	@Override
	@Transactional(readOnly = true)
	public Realm getDefaultRealm() {
		return get("defaultRealm", true, Realm.class);
	}

	@Override
	@Transactional
	public Realm setDefaultRealm(Realm realm) {

		realm.setDefaultRealm(true);

		for (Realm r : allRealms()) {
			if (!r.equals(realm)) {
				r.setDefaultRealm(false);
			}
			save(r);
		}
		save(realm);
		flush();
		refresh(realm);

		return realm;
	}


}
