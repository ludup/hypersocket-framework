package com.hypersocket.scheduler;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class SchedulerResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<SchedulerResource> implements
		SchedulerResourceRepository {

	@Override
	protected Class<SchedulerResource> getResourceClass() {
		return SchedulerResource.class;
	}

}
