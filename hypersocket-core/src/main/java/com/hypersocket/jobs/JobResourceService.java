package com.hypersocket.jobs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface JobResourceService extends
		AbstractResourceService<JobResource> {

	JobResource updateResource(JobResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	JobResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(JobResource resource)
			throws AccessDeniedException;

	String createJob() throws ResourceException, AccessDeniedException;
	
	String createJob(String parent) throws ResourceException, AccessDeniedException;
	
	void reportJobStarting(String uuid) throws ResourceNotFoundException, InvalidJobStateException;

	void reportJobComplete(String uuid, String result) throws ResourceNotFoundException, InvalidJobStateException;

	void reportJobFailed(String uuid, Throwable t) throws ResourceNotFoundException, InvalidJobStateException;

	boolean isJobActive(String uuid) throws ResourceNotFoundException, InvalidJobStateException;

	void reportJobFailed(String uuid, String result) throws ResourceNotFoundException, InvalidJobStateException;

	boolean hasActiveJobs(String parent) throws ResourceNotFoundException;

	void waitForCompletion(String uuid, long timeout)
			throws TimeoutException, ResourceNotFoundException, InterruptedException;

	Collection<JobResource> getJobs(String jobUuid);

}
