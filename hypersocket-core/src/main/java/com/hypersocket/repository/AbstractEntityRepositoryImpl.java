package com.hypersocket.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractEntityRepositoryImpl<T extends AbstractEntity<K>,K> extends AbstractRepositoryImpl<K> implements AbstractEntityRepository<T,K> {

	
	protected abstract Class<T> getEntityClass();
	
	protected AbstractEntityRepositoryImpl(boolean requiresDemoWrite) {
		super(requiresDemoWrite);
	}
	
	protected AbstractEntityRepositoryImpl() {
		super();
	}
	
	@Override
	@Transactional
	public void saveEntity(T protocol) {
		save(protocol);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<T> allEntities() {
		return allEntities(getEntityClass(), new DeletedCriteria(false));
	}
	
	@Override
	@Transactional(readOnly=true)
	public T getEntityById(Long id) {
		return get("id", id, getEntityClass(), new DeletedCriteria(false));
	}

	@Override
	@Transactional
	public void deleteEntity(T entity) {
		delete(entity);
	}

}
