package com.hypersocket.resource;

import java.util.Iterator;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends RealmResource>
		extends AbstractSimpleResourceRepositoryImpl<T> implements
		AbstractResourceRepository<T> {

	static Logger log = org.slf4j.LoggerFactory.getLogger(AbstractResourceRepositoryImpl.class);
	
	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		deleteResourcesOfClassFromRealm(realm, getResourceClass());
	}

	protected void deleteResourcesOfClassFromRealm(Realm realm, Class<?> clazz) {
		log.info(String.format("Deleting %s", clazz));
		
		int count = 0;
		for(Iterator<?> it = iterate(clazz, new ColumnSort[0], new RealmCriteria(realm)); it.hasNext(); ) {
			it.next();
			it.remove();
			count++;
			if(count % 1000 == 0)
				flush();
		}
		flush();		
		log.info(String.format("Deleted %d %s entries", count, clazz));
	}
	
	@Override
	public boolean isDeletable() {
		return true;
	}
}
