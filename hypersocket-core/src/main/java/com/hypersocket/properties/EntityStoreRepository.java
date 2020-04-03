package com.hypersocket.properties;

import com.hypersocket.resource.FindableResourceRepository;
import com.hypersocket.resource.SimpleResource;

public interface EntityStoreRepository<T extends SimpleResource> extends FindableResourceRepository<T> {

	T createEntity(Object resource);
	
	Class<?> getEntityClass();
	
}
