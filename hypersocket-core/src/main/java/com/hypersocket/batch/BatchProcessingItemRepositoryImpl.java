package com.hypersocket.batch;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmResource;

public abstract class BatchProcessingItemRepositoryImpl<T extends RealmResource> 
		extends AbstractResourceRepositoryImpl<T> implements BatchProcessingItemRepository<T> {


	@Override
	@Transactional(readOnly=true)
	public Collection<T> allResources() {
		return list(getResourceClass());
	}

}
