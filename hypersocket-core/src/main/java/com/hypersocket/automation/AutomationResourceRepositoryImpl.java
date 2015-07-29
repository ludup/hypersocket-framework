package com.hypersocket.automation;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class AutomationResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<AutomationResource> implements
		AutomationResourceRepository {

	@Override
	protected Class<AutomationResource> getResourceClass() {
		return AutomationResource.class;
	}

	@Override
	protected void processDefaultCriteria(Criteria criteria) {
		criteria.setFetchMode("triggers", FetchMode.SELECT);
	}

	@Override
	protected void processDefaultCriteria(DetachedCriteria criteria) {
		criteria.setFetchMode("triggers", FetchMode.SELECT);
	}

}
