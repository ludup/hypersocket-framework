package com.hypersocket.batch;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.AbstractRepository;

public interface BatchProcessingItemRepository<T extends AbstractEntity<Long>> extends AbstractRepository<Long> {

	T saveItem(T object);

	void deleteRealm(Realm realm);
	
	Class<? extends T> getEntityClass();

	void markAllAsDeleted(List<Long> excludeRealms, boolean deleted);
}
