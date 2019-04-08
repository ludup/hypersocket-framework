package com.hypersocket.batch;

import java.util.Collection;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmResource;

public abstract class BatchProcessingItemRepositoryImpl<T extends RealmResource> 
		extends AbstractResourceRepositoryImpl<T> implements BatchProcessingItemRepository<T> {

	
	@Transactional
	@Override
	public Collection<T> getAllResourcesAndMarkDeleted() {
		Collection<T> results = list(getResourceClass(), new DeletedCriteria(false));
		
		if(!results.isEmpty()) {
			Query query = getCurrentSession().createQuery("UPDATE EmailBatchItem b SET b.deleted = :del WHERE b.id in (:idList)");
			query.setParameter("del", true);
			query.setParameterList("idList", ResourceUtils.createResourceIdArray(results));
			query.executeUpdate();
		}
		
		return results;
	}
}
