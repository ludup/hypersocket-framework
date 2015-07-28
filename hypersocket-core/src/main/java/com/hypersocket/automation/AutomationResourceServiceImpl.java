package com.hypersocket.automation;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.hypersocket.automation.events.AutomationResourceCreatedEvent;
import com.hypersocket.automation.events.AutomationResourceDeletedEvent;
import com.hypersocket.automation.events.AutomationResourceEvent;
import com.hypersocket.automation.events.AutomationResourceUpdatedEvent;
import com.hypersocket.automation.events.AutomationTaskFinishedEvent;
import com.hypersocket.automation.events.AutomationTaskStartedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;

@Service
public class AutomationResourceServiceImpl extends
		AbstractResourceServiceImpl<AutomationResource> implements
		AutomationResourceService, ApplicationListener<ContextStartedEvent> {

	private static Logger log = LoggerFactory
			.getLogger(AutomationResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "AutomationResourceService";

	private Map<Long, String> scheduleIdsByResource = new HashMap<Long, String>();

	@Autowired
	AutomationResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@Autowired
	EntityResourcePropertyStore entityPropertyStore;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	RealmRepository realmRepository;

	public AutomationResourceServiceImpl() {
		super("automationResource");
	}
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.automation");

		for (AutomationResourcePermission p : AutomationResourcePermission
				.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("automationTemplate.xml");

		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(AutomationResourceEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceUpdatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceDeletedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationTaskStartedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AutomationTaskFinishedEvent.class,
				RESOURCE_BUNDLE);
		entityPropertyStore.registerResourceService(AutomationResource.class,
				repository);
	}

	@Override
	protected AbstractResourceRepository<AutomationResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<AutomationResourcePermission> getPermissionType() {
		return AutomationResourcePermission.class;
	}
	
	protected Class<AutomationResource> getResourceClass() {
		return AutomationResource.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	protected void afterDeleteResource(AutomationResource resource)
			throws ResourceChangeException {
		try {
			unschedule(resource);

		} catch (SchedulerException e) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.couldNotUnschedule", resource.getName(),
					e.getMessage());
		}
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource) {

		eventService.publishEvent(new AutomationResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public AutomationResource updateResource(AutomationResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);

		updateResource(resource, properties, new TransactionAdapter<AutomationResource>() {

			@Override
			public void afterOperation(AutomationResource resource,
					Map<String, String> properties) {
				setProperties(resource, properties);
				schedule(resource);
			}
		});

		return resource;
	}

	private void setProperties(AutomationResource resource, Map<String,String> properties) {
		TaskProvider provider = taskService.getTaskProvider(resource);
		for(String resourceKey : provider.getPropertyNames(resource)) {
			if(properties.containsKey(resourceKey)) {
				provider.getRepository().setValue(resource, resourceKey, properties.get(resourceKey));
			}
		}
	}

	protected Date calculateDateTime(Date from, String time) {

		Calendar c = Calendar.getInstance();

		Date ret = null;

		if (from != null) {
			c.setTime(from);
			ret = c.getTime();
		}

		if (!StringUtils.isEmpty(time)) {
			int idx = time.indexOf(':');
			c.set(Calendar.HOUR_OF_DAY,
					Integer.parseInt(time.substring(0, idx)));
			c.set(Calendar.MINUTE, Integer.parseInt(time.substring(idx + 1)));
			ret = c.getTime();
		}

		return ret;
	}

	protected void unschedule(AutomationResource resource)
			throws SchedulerException {

		if (scheduleIdsByResource.containsKey(resource.getId())) {
			String scheduleId = scheduleIdsByResource.remove(resource.getId());
			schedulerService.cancelNow(scheduleId);
		}
	}

	protected void schedule(AutomationResource resource) {

		Date start = calculateDateTime(resource.getStartDate(),
				resource.getStartTime());
		Date end = calculateDateTime(resource.getEndDate(),
				resource.getEndTime());

		int interval = 0;
		int repeat = -1;

		if (resource.getRepeatValue() > 0) {

			switch (resource.getRepeatType()) {
			case DAYS:
				interval = resource.getRepeatValue() * (60000 * 60 * 24);
				break;
			case HOURS:
				interval = resource.getRepeatValue() * (60000 * 60);
				break;
			case MINUTES:
				interval = resource.getRepeatValue() * 60000;
				break;
			case SECONDS:
				interval = resource.getRepeatValue() * 1000;
				break;
			case NEVER:
			default:
				interval = 0;
				repeat = 0;
				break;
			}
		}

		PermissionsAwareJobData data = new PermissionsAwareJobData(
				resource.getRealm(), resource.getName());
		data.put("resourceId", resource.getId());

		try {

			String scheduleId;

			if (scheduleIdsByResource.containsKey(resource.getId())) {

				scheduleId = scheduleIdsByResource.get(resource.getId());

				try {
					if (start == null || start.before(new Date())) {
						schedulerService.rescheduleNow(scheduleId, interval,
								repeat, end);
					} else {
						schedulerService.rescheduleAt(scheduleId, start,
								interval, repeat, end);
					}
					return;
				} catch (NotScheduledException e) {
					if (log.isInfoEnabled()) {
						log.info("Attempted to reschedule job but it was not scheduled.");
					}
					scheduleIdsByResource.remove(resource.getId());
				}

			}

			if (start == null || start.before(new Date())) {
				scheduleId = schedulerService.scheduleNow(AutomationJob.class,
						data, interval, repeat, end);
			} else {
				scheduleId = schedulerService.scheduleAt(AutomationJob.class,
						data, start, interval, repeat, end);
			}

			scheduleIdsByResource.put(resource.getId(), scheduleId);

		} catch (SchedulerException e) {
			log.error(
					"Failed to schedule automation task " + resource.getName(),
					e);
		}
	}

	@Override
	public AutomationResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		AutomationResource resource = new AutomationResource();
		resource.setName(name);
		resource.setRealm(realm);

		createResource(resource, properties, new TransactionAdapter<AutomationResource>() {

			@Override
			public void afterOperation(AutomationResource resource,
					Map<String, String> properties) {
				setProperties(resource, properties);
				schedule(resource);
			}
		});

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(String resourceKey)
			throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		TaskProvider provider = taskService.getTaskProvider(resourceKey);
		Collection<PropertyCategory> results = provider.getRepository()
				.getPropertyCategories(null);

		results.addAll(repository.getPropertyCategories(null));

		return results;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			AutomationResource resource) throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		TaskProvider provider = taskService.getTaskProvider(resource);
		Collection<PropertyCategory> results = provider.getRepository()
				.getPropertyCategories(resource);

		results.addAll(repository.getPropertyCategories(resource));

		return results;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {
		throw new IllegalStateException(
				"AutomationResource needs provider resource key to return property templates");
	}

	@Override
	public Collection<String> getTasks() throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		return taskService.getAutomationTasks();
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {

		if (log.isInfoEnabled()) {
			log.info("Scheduling one time only or repetitive automation resources");
		}

		for (Realm realm : realmRepository.allRealms()) {
			for (AutomationResource resource : repository.getResources(realm)) {
				if (!resource.isDailyJob()) {
					schedule(resource);
				}
			}
		}

		scheduleDailyJobs();
	}

	@Override
	public void scheduleDailyJobs() {

		if (log.isInfoEnabled()) {
			log.info("Scheduling daily automation resources");
		}

		for (Realm realm : realmRepository.allRealms()) {
			for (AutomationResource resource : repository.getResources(realm)) {
				if (resource.isDailyJob()) {
					schedule(resource);
				}
			}
		}

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.add(Calendar.DAY_OF_MONTH, 1);
		JobDataMap data = new JobDataMap();
		data.put("jobName", "automationDailyJob");
		try {
			schedulerService.scheduleAt(DailySchedulerJob.class, data,
					c.getTime());
		} catch (SchedulerException e) {
			log.error("Failed to schedule daily automation jobs", e);
		}

	}
}
