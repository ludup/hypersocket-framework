package com.hypersocket.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.jobs.events.JobResourceCreatedEvent;
import com.hypersocket.jobs.events.JobResourceDeletedEvent;
import com.hypersocket.jobs.events.JobResourceEvent;
import com.hypersocket.jobs.events.JobResourceUpdatedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;


@Service
public class JobResourceServiceImpl extends
		AbstractResourceServiceImpl<JobResource> implements
		JobResourceService {

	public static final String RESOURCE_BUNDLE = "JobResourceService";

	@Autowired
	JobResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	public JobResourceServiceImpl() {
		super("Job");
	}
	
	@PostConstruct
	private void postConstruct() {

		setAssertPermissions(false);
		
		i18nService.registerBundle(RESOURCE_BUNDLE);

		repository.loadPropertyTemplates("jobResourceTemplate.xml");

		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(
				JobResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				JobResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				JobResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				JobResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);

		repository.getEntityStore().registerResourceService(JobResource.class, repository);
	}

	@Override
	protected AbstractResourceRepository<JobResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return null;
	}
	
	protected Class<JobResource> getResourceClass() {
		return JobResource.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(JobResource resource) {
		eventService.publishEvent(new JobResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(JobResource resource,
			Throwable t) {
		eventService.publishEvent(new JobResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(JobResource resource) {
		eventService.publishEvent(new JobResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(JobResource resource,
			Throwable t) {
		eventService.publishEvent(new JobResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(JobResource resource) {
		eventService.publishEvent(new JobResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(JobResource resource,
			Throwable t) {
		eventService.publishEvent(new JobResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public JobResource updateResource(JobResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);

		updateResource(resource, properties);

		return resource;
	}

	@Override
	public JobResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		JobResource resource = new JobResource();
		resource.setName(name);
		resource.setRealm(realm);
		resource.setState(JobState.SCHEDULED);

		createResource(resource, properties);

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {
		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			JobResource resource) throws AccessDeniedException {
		return repository.getPropertyCategories(resource);
	}

	@SuppressWarnings("unchecked")
	protected void updateState(String uuid, JobState current, JobState updated, String... result)  throws ResourceNotFoundException, InvalidJobStateException {
		JobResource job = getResourceByName(uuid);
		if(job.getState()!=current) {
			throw new InvalidJobStateException();
		}
		job.setState(updated);
		repository.saveResource(job, null);
		repository.flush();
	}
	
	@Override
	public void reportJobStarting(String uuid) throws ResourceNotFoundException, InvalidJobStateException {
		updateState(uuid, JobState.SCHEDULED, JobState.IN_PROGRESS);
	}

	@Override
	public void reportJobComplete(String uuid, String result) throws ResourceNotFoundException, InvalidJobStateException {
		updateState(uuid, JobState.IN_PROGRESS, JobState.COMPLETE, result);
	}

	@Override
	public void reportJobFailed(String uuid, Throwable t) throws ResourceNotFoundException, InvalidJobStateException {
		StringWriter str = new StringWriter();
		PrintWriter w = new PrintWriter(str);
		t.printStackTrace(w);
		updateState(uuid, JobState.IN_PROGRESS, JobState.ERROR, StringUtils.abbreviate(str.getBuffer().toString(), 8000));
	}

	@Override
	public void reportJobFailed(String uuid, String result) throws ResourceNotFoundException, InvalidJobStateException {
		updateState(uuid, JobState.IN_PROGRESS, JobState.FAILED, result);
	}
	
	@Override
	public boolean isJobActive(String uuid) throws ResourceNotFoundException {
		JobResource job = getResourceByName(uuid);
		return isJobActive(job);
	}
	
	private boolean isJobActive(JobResource job) {
		switch(job.getState()) {
		case SCHEDULED:
		case IN_PROGRESS:
		{
			return true;
		}
		default:
			return false;
		}
	}
	
	@Override
	public boolean hasActiveJobs(String parent) throws ResourceNotFoundException {
		
		for(JobResource child : repository.getChildJobs(parent)) {
			if(isJobActive(child)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void waitForCompletion(String uuid, long timeout) throws TimeoutException, ResourceNotFoundException, InterruptedException {
		
		long started = System.currentTimeMillis();
		
		Collection<JobResource> children = repository.getChildJobs(uuid);
		if(children.isEmpty()) {
			/**
			 * Just wait on the job
			 */
			while(System.currentTimeMillis()-started < timeout) {
				Thread.sleep(1000);
				
				if(!isJobActive(uuid)) {
					return;
				}
			}
			
			throw new TimeoutException();
		} else {
			
			while(System.currentTimeMillis()-started < timeout) {
				Thread.sleep(1000);
				
				boolean active = false;
				for(JobResource child : repository.getChildJobs(uuid)) {
					if(isJobActive(child)) {
						active = true;
						break;
					}
				}
				
				if(!active) {
					return;
				}
			}
			
			throw new TimeoutException();
		}
	}

	@Override
	public String createJob() throws ResourceCreationException, AccessDeniedException, ResourceNotFoundException {
		return createJob(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String createJob(String parent) throws ResourceCreationException, AccessDeniedException, ResourceNotFoundException {
		
		JobResource parentJob = null;
		
		if(parent!=null) {
			parentJob = getResourceByName(parent);
		}
		
		JobResource job = new JobResource();
		job.setName(UUID.randomUUID().toString());
		job.setRealm(getCurrentRealm());
		job.setState(JobState.SCHEDULED);
		job.setParentJob(parentJob);
		
		repository.saveResource(job, null);
		repository.flush();
		return job.getName();
	}

	@Override
	public Collection<JobResource> getJobs(String jobUuid) {
		return repository.getChildJobs(jobUuid);
	}
}
