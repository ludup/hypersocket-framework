package com.hypersocket.repository;

import java.util.List;

public abstract class AbstractEntityRepositoryImpl<T extends AbstractEntity<K>,K> extends AbstractRepositoryImpl<K> implements AbstractEntityRepository<T,K> {

	
	protected abstract Class<T> getEntityClass();
	
	@Override
	public void saveEntity(T protocol) {
		save(protocol);
	}
	
	@Override
	public List<T> allEntities() {
		return allEntities(getEntityClass(), new DeletedCriteria(false));
	}
	
	@Override
	public T getEntityById(Long id) {
		return get("id", id, getEntityClass(), new DeletedCriteria(false));
	}

	@Override
	public void deleteEntity(T entity) {
		delete(entity);
	}

}
