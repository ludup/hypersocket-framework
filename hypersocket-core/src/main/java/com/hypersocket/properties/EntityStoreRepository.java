package com.hypersocket.properties;

import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.FindableResourceRepository;

public interface EntityStoreRepository<T extends AbstractResource> extends FindableResourceRepository<T> {

	T createEntity(Object resource);
	
	Class<?> getEntityClass();
	
}
