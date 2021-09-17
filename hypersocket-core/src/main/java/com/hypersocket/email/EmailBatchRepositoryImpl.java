package com.hypersocket.email;

import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.batch.BatchProcessingItemRepositoryImpl;

public class EmailBatchRepositoryImpl extends BatchProcessingItemRepositoryImpl<EmailBatchItem> implements EmailBatchRepository {

	@Override
	public Class<EmailBatchItem> getEntityClass() {
		return EmailBatchItem.class;
	}

	@Override
	@Transactional
	public void markAllAsDeleted(List<Long> exclude, boolean deleted) {
		Query q;
		if(exclude.isEmpty()) {
			q = createQuery(String.format("update %s ent set ent.deleted = :r", getEntityClass().getSimpleName()), true);
			q.setParameter("r", deleted);
		}
		else {
			q = createQuery(String.format("update %s ent set ent.deleted = :r where ent.realm not in :l", getEntityClass().getSimpleName()), true);
			q.setParameter("r", deleted);
			q.setParameterList("l", exclude);
		}
		q.executeUpdate();
		flush();
	}
}
