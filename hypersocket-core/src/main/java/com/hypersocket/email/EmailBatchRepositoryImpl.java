package com.hypersocket.email;

import com.hypersocket.batch.BatchProcessingItemRepositoryImpl;

public class EmailBatchRepositoryImpl extends BatchProcessingItemRepositoryImpl<EmailBatchItem> implements EmailBatchRepository {

	@Override
	public Class<EmailBatchItem> getEntityClass() {
		return EmailBatchItem.class;
	}
}
