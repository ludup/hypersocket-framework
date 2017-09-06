package com.hypersocket.resource;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends RealmResource>
		extends AbstractSimpleResourceRepositoryImpl<T> implements
		AbstractResourceRepository<T> {

	@Override
	@Transactional
	public void clearRealm(Realm realm) {
		Query q = createQuery(String.format("delete from %s where realm = :r", getResourceClass().getSimpleName()), true);
		q.setParameter("r", realm);
		q.executeUpdate();
	}
}
