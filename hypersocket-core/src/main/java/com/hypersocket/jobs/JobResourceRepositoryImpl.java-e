package com.hypersocket.jobs;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class JobResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<JobResource> implements
		JobResourceRepository {

	@Override
	protected Class<JobResource> getResourceClass() {
		return JobResource.class;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<JobResource> getChildJobs(final String parent) {
		return list(JobResource.class, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("parentJob");
				criteria.add(Restrictions.eq("name", parent));
			}
		});
	}

}
