package com.hypersocket.batch;

import java.util.Collection;

import com.hypersocket.resource.AbstractResourceRepository;

public interface BatchProcessingItemRepository extends AbstractResourceRepository<BatchItem> {

	Collection<BatchItem> allResources();

}
