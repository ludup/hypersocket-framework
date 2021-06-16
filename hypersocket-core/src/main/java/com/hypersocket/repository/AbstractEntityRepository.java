package com.hypersocket.repository;

import java.util.List;

public interface AbstractEntityRepository<T,K> extends AbstractRepository<K> {

	void saveEntity(T protocol);

	List<T> allEntities();

	T getEntityById(Long id);

	T getEntityById(Long id, boolean includeDeleted);

	void deleteEntity(T entity);

}
