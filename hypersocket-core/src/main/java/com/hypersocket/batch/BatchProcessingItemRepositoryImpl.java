package com.hypersocket.batch;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.resource.ResourceException;

public abstract class BatchProcessingItemRepositoryImpl<T extends AbstractEntity<Long>> 
		extends AbstractRepositoryImpl<Long> implements BatchProcessingItemRepository<T> {
	
	@Autowired
	private RealmService realmService;

	@PostConstruct
	private void setup() {
		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {
				deleteRealm(realm);
			}
			
		});
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		Query q = createQuery(String.format("delete from %s where realm = :r", getEntityClass().getSimpleName()), true);
		q.setParameter("r", realm);
		q.executeUpdate();
		flush();
	}
	
	@Override
	public abstract Class<? extends T> getEntityClass();

	@SuppressWarnings("unchecked")
	@Override
	public T saveItem(T entity) {
		return (T) save(entity);
	}

	@Override
	@Transactional
	public void markAllAsDeleted() {
		Query q = createQuery(String.format("update %s set deleted = :r", getEntityClass().getSimpleName()), true);
		q.setParameter("r", true);
		q.executeUpdate();
		flush();
	}
}
