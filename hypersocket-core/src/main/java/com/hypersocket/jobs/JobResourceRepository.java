package com.hypersocket.jobs;

import java.util.Collection;

import com.hypersocket.resource.AbstractResourceRepository;

public interface JobResourceRepository extends
		AbstractResourceRepository<JobResource> {

	Collection<JobResource> getChildJobs(String parent);

}
