package com.hypersocket.properties;

import com.hypersocket.resource.SimpleResource;
import com.hypersocket.resource.FindableResourceRepository;

public interface EntityStoreRepository<T extends SimpleResource> extends FindableResourceRepository<T> {

	T createEntity(Object resource);
	
	Class<?> getEntityClass();
	
}
