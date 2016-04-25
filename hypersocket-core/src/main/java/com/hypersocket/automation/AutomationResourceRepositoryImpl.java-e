package com.hypersocket.automation;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
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

}
