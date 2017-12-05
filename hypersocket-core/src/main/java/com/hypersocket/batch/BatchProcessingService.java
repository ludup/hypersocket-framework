package com.hypersocket.batch;

import com.hypersocket.resource.RealmResource;

public interface BatchProcessingService<T extends RealmResource> {

	void processBatchItems();

}
