package com.hypersocket.tasks.count;

import java.util.Collection;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface CountKeyRepository extends AbstractResourceRepository<CountKey> {

	CountKey getCountKey(Realm realm, String resourceKey);

	Collection<CountKey> getCountValues(Realm realm, String[] resourceKeys);

}
