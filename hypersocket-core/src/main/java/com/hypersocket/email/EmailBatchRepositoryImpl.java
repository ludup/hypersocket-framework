package com.hypersocket.email;

import com.hypersocket.batch.BatchProcessingItemRepositoryImpl;
import com.hypersocket.realm.Realm;

public class EmailBatchRepositoryImpl extends BatchProcessingItemRepositoryImpl<EmailBatchItem> implements EmailBatchRepository {

	@Override
	protected Class<EmailBatchItem> getResourceClass() {
		return EmailBatchItem.class;
	}

	@Override
	public void deleteRealm(Realm realm) {
		// TODO Auto-generated method stub
		super.deleteRealm(realm);
	}
}
