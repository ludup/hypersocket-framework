package com.hypersocket.email;

import com.hypersocket.batch.BatchProcessingItemRepositoryImpl;

public class EmailBatchRepositoryImpl extends BatchProcessingItemRepositoryImpl<EmailBatchItem> implements EmailBatchRepository {

	@Override
	protected Class<EmailBatchItem> getResourceClass() {
		return EmailBatchItem.class;
	}

}
