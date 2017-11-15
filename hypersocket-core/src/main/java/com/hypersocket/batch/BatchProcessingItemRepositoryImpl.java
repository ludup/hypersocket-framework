package com.hypersocket.batch;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

public abstract class BatchProcessingItemRepositoryImpl extends AbstractResourceRepositoryImpl<BatchItem> implements BatchProcessingItemRepository {

	@Override
	protected Class<BatchItem> getResourceClass() {
		return BatchItem.class;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<BatchItem> allResources() {
		return allEntities(BatchItem.class);
	}

}
