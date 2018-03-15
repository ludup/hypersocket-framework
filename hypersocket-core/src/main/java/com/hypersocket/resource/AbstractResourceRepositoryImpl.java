package com.hypersocket.resource;

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
		
		log.info(String.format("Deleting %s", getClass().getName()));
		
		int count = 0;
		for(T resource : getResources(realm)) {
			delete(resource);
			count++;
		}
		flush();		
		log.info(String.format("Deleted %d %s entries", count, getResourceClass().getSimpleName()));
	}
	
	@Override
	public boolean isDeletable() {
		return true;
	}
}
