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
	public void deleteRealm(Realm realm) {
		String hql = String.format("delete from %s where realm = :r", getResourceClass().getSimpleName());
		Query q = createQuery(hql, true);
		q.setParameter("r", realm);
		log.info(String.format("Deleted %d %s entries", q.executeUpdate(), getResourceClass().getSimpleName()));
		flush();
	}
	
	@Override
	public boolean isDeletable() {
		return true;
	}
}
