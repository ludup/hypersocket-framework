package com.hypersocket.resource;

import com.hypersocket.realm.Realm;

public interface AbstractResourceRepository<T extends RealmResource> extends AbstractSimpleResourceRepository<T>, FindableResourceRepository<T> {

	void clearRealm(Realm realm);

}
