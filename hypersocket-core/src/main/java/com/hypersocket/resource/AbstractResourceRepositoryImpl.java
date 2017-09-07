package com.hypersocket.resource;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends RealmResource>
		extends AbstractSimpleResourceRepositoryImpl<T> implements
		AbstractResourceRepository<T> {

	static Logger log = org.slf4j.LoggerFactory.getLogger(AbstractResourceRepositoryImpl.class);
	
	@Override
	@Transactional
	public void clearRealm(Realm realm) {
		log.info(getResourceClass().getSimpleName());
		Query q = createQuery(String.format("delete from %s where realm = :r", getResourceClass().getSimpleName()), true);
		q.setParameter("r", realm);
		q.executeUpdate();
	}
}
