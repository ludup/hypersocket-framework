package com.hypersocket.batch;

import java.util.Collection;

import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.RealmResource;

public interface BatchProcessingItemRepository<T extends RealmResource> extends AbstractResourceRepository<T> {

	Collection<T> allResources();

}
