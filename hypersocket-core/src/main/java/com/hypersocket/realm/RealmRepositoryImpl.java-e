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

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.resource.HiddenFilter;

@Repository
@Transactional
public class RealmRepositoryImpl extends AbstractRepositoryImpl<Long> implements RealmRepository {

	
	@Override
	public Realm createRealm(String name, String module, Map<String,String> properties, RealmProvider provider) {
		Realm realm = new Realm();
		realm.setName(name);
		realm.setResourceCategory(module);
		save(realm);
		
		if(!properties.isEmpty()) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				provider.setValue(realm, e.getKey(), e.getValue());
			}
			refresh(realm);
		}
		
		return realm;
	}
	
	@Override
	public Realm saveRealm(Realm realm, Map<String,String> properties, RealmProvider provider) {
		
		boolean isNew = realm.getId()==null;
		save(realm);
		
		for (Map.Entry<String, String> e : properties.entrySet()) {
			provider.setValue(realm, e.getKey(), e.getValue());
		}
		
		if(!isNew) {
			refresh(realm);
		}
		
		return realm;
	}
	
	@Override
	public List<Realm> allRealms() {
		return allEntities(Realm.class, new HiddenFilter(), new DeletedCriteria(false), new DistinctRootEntity());
	}
	
	@Override
	public List<Realm> allRealms(String resourceKey) {
		return list("resourceCategory", resourceKey, Realm.class, new HiddenFilter(), new DeletedCriteria(false), new DistinctRootEntity());
	}
	
	protected Realm getRealm(String column, Object value) {
		return get(column, value, Realm.class, true);
	}
	
	@Override
	public Realm getRealmById(Long id) {
		return get("id", id, Realm.class);
	}
	
	@Override
	public Realm getRealmByName(String name) {
		return getRealm("name", name);
	}
	
	@Override
	public Realm getRealmByName(String name, boolean deleted) {
		return get("name", name, Realm.class, new DeletedCriteria(deleted));
	}

	@Override
	public Realm getRealmByHost(String host) {
		return getRealm("host", host);
	}

	@Override
	public void delete(Realm realm) {
		realm.setDeleted(true);
		realm.setName(realm.getName() + "[#" + realm.getId() + " deleted]");
		
		save(realm);
	}
}
