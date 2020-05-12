package com.hypersocket.batch;

import com.hypersocket.repository.AbstractEntity;

public interface BatchProcessingService<T extends AbstractEntity<?>> {

	void processBatchItems();

}
