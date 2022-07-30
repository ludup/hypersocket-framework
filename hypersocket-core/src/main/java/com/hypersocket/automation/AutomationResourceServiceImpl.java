package com.hypersocket.automation;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PostConstruct;

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
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.SessionService;
import com.hypersocket.tasks.TaskDefinition;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceRepository;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResultType;
import com.hypersocket.triggers.TriggerType;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class AutomationResourceServiceImpl extends AbstractResourceServiceImpl<AutomationResource>
		implements AutomationResourceService, ApplicationListener<ContextStartedEvent> {

	private static Logger log = LoggerFactory.getLogger(AutomationResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "AutomationResourceService";

	@Autowired
	private AutomationResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private RealmRepository realmRepository;

	@Autowired
	private TriggerResourceService triggerService;

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private ClusteredSchedulerService schedulerService;
	
	@Autowired
	private SchedulingResourceService resourceScheduler;
	
	@Autowired
	private TriggerResourceRepository triggerRepository; 
	
	private AutomationController controller;
	
	public AutomationResourceServiceImpl() {
		super("automationResource");
	}

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.automation");

		for (AutomationResourcePermission p : AutomationResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("automationTemplate.xml");

		/**
		 * Register the events. All events have to be registered so the system
		 * knows about them.
		 */
		eventService.registerEvent(AutomationResourceEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceCreatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceUpdatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceDeletedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationTaskStartedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(AutomationTaskFinishedEvent.class, RESOURCE_BUNDLE);
		
		EntityResourcePropertyStore.registerResourceService(AutomationResource.class, repository);
		
		realmService.registerRealmListener(new RealmAdapter() {
			@Override
			public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {
				getRepository().deleteRealm(realm);
			}	
		});
	}

	@Override
	public void setController(AutomationController controller) {
		this.controller = controller;
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
	protected boolean checkUnique(AutomationResource resource, boolean create) throws AccessDeniedException {
		if(super.checkUnique(resource, create)) {
			try {
				triggerService.getResourceByName(resource.getName());
				return false;
			} catch (ResourceNotFoundException e) {
				return true;
			} 
		}
		return false;
	}

	@Override
	protected void fireResourceCreationEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(AutomationResource resource, Throwable t) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource, Throwable t) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	protected void afterDeleteResource(AutomationResource resource) throws ResourceException {
		try {
			resourceScheduler.unschedule(resource);

		} catch (SchedulerException e) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.couldNotUnschedule", resource.getName(),
					e.getMessage());
		}
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource) {

		eventService.publishEvent(new AutomationResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource, Throwable t) {
		eventService.publishEvent(new AutomationResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public AutomationResource updateResource(AutomationResource resource, String name, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

		resource.setName(name);

		updateResource(resource, properties, new TransactionAdapter<AutomationResource>() {

			@Override
			public void afterOperation(AutomationResource resource, Map<String, String> properties) {
				setProperties(resource, properties);
				schedule(resource);
			}
		});

		return resource;
	}

	protected void schedule(AutomationResource resource) {
		resourceScheduler.schedule(resource, resource.getStartDate(), resource.getStartTime(), 
				resource.getEndDate(), resource.getEndTime(), resource.getRepeatType(), resource.getRepeatValue(), AutomationJob.class);
	}
	
	private void setProperties(AutomationResource resource, Map<String, String> properties) {
		TaskProvider provider = taskService.getTaskProvider(resource);
		for (String resourceKey : provider.getPropertyNames(resource)) {
			if (properties.containsKey(resourceKey)) {
				provider.getRepository().setValue(resource, resourceKey, properties.get(resourceKey));
			}
		}
	}
	
	@Override
	public void runNow(AutomationResource resource) throws SchedulerException {
		
		PermissionsAwareJobData data = new PermissionsAwareJobData(resource.getRealm(), resource.getName());
		data.put("resourceId", resource.getId());
		
		String scheduleId = String.format("run_now_%s_%s", UUID.randomUUID().toString(),resource.getId().toString());
		
		schedulerService.scheduleNow(AutomationJob.class, scheduleId, data);
	}

	

	@Override
	public AutomationResource createResource(String name, Realm realm, String resourceKey, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

		AutomationResource resource = new AutomationResource();
		resource.setName(name);
		resource.setRealm(realm);
		resource.setResourceKey(resourceKey);

		createResource(resource, properties, new TransactionAdapter<AutomationResource>() {

			@Override
			public void afterOperation(AutomationResource resource, Map<String, String> properties) {
				setProperties(resource, properties);
				schedule(resource);
			}
		});

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(String resourceKey) throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		TaskProvider provider = taskService.getTaskProvider(resourceKey);
		Collection<PropertyCategory> results = provider.getRepository().getPropertyCategories(null);

		results.addAll(repository.getPropertyCategories(null));

		return results;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(AutomationResource resource) throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		TaskProvider provider = taskService.getTaskProvider(resource);
		Collection<PropertyCategory> results = provider.getRepository().getPropertyCategories(resource);

		results.addAll(repository.getPropertyCategories(resource));

		return results;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException {
		throw new IllegalStateException("AutomationResource needs provider resource key to return property templates");
	}

	@SuppressWarnings("unchecked")
	@Override
	public AutomationResource createTrigger(String name,
			String event, 
			TriggerResultType result, 
			String task,
			Map<String, String> properties,
			Realm realm,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, 
			final TriggerResource parent,
			final AutomationResource automation)
			throws ResourceException, AccessDeniedException {
		
		triggerService.createResource(name, TriggerType.AUTOMATION, event,
				result, task, properties, realm, allConditions,
				anyConditions, parent, automation.getId(), false, new TransactionAdapter<TriggerResource>() {
			@Override
			public void afterOperation(TriggerResource resource, Map<String, String> properties) {
				
				if(parent==null) {
					automation.getChildTriggers().add(resource);
					try {
						updateResource(automation);
					} catch (ResourceException e) {
						throw new IllegalStateException(e.getMessage(), e);
					} catch (AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
			}
		});
		
		repository.refresh(automation);
		return automation;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public AutomationResource updateTrigger(TriggerResource trigger, String name, String event,
			TriggerResultType result, String task, Map<String, String> properties, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, final TriggerResource parent, final AutomationResource automation)
					throws ResourceException, AccessDeniedException {
		
		triggerService.updateResource(trigger, name, TriggerType.AUTOMATION, event,
				result, task, properties, allConditions,
				anyConditions, parent, automation.getId(), false);
		
		repository.refresh(automation);
		return automation;
	}

	@Override
	public Collection<TaskDefinition> getTasks() throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		return taskService.getAutomationTasks();
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {

		if (log.isInfoEnabled()) {
			log.info("Scheduling one time only or repetitive automation resources");
		}

		sessionService.runAsSystemContext(() ->  {
				for (Realm realm : realmRepository.allRealms()) {
					for (AutomationResource resource : repository.getResources(realm)) {
						if (!resource.isDailyJob()) {
							schedule(resource);
						}
					}
				}
				scheduleDailyJobs();
		});
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

		try {
			if(schedulerService.jobDoesNotExists("automationDailyJob")){
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR, 0);
				c.set(Calendar.MINUTE, 0);
				c.add(Calendar.DAY_OF_MONTH, 1);
				JobDataMap data = new JobDataMap();
				data.put("jobName", "automationDailyJob");
				
				schedulerService.scheduleAt(DailySchedulerJob.class, "automationDailyJob", data, c.getTime(), HypersocketUtils.ONE_DAY);
			}
		} catch (SchedulerException e) {
			log.error("Failed to schedule daily automation jobs", e);
		}

	}
	protected boolean isExportingAdditionalProperties() {
		return false;
	}
	
	protected void performImport(AutomationResource resource, Realm realm)
			throws ResourceException, AccessDeniedException {

		Map<String, String> properties = resource.getProperties();
		resource.setProperties(null);

		super.performImport(resource, realm);

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.getRepository().setValues(resource, properties);

		TriggerResource parentTrigger = null;
		for (TriggerResource child : resource.getChildTriggers()) {
			if(parentTrigger == null) {
				parentTrigger = child;
			} else {
				child.setParentTrigger(parentTrigger);
				parentTrigger = child;
			}
			performImport(child, realm);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void performImport(TriggerResource resource, Realm realm)
			throws ResourceException, AccessDeniedException {

		Map<String, String> properties = resource.getProperties();
		resource.setProperties(null);

		resource.setRealm(realm);
		resource.setTriggerType(TriggerType.AUTOMATION);
		
		triggerService.checkImportName(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
		triggerService.createResource(resource, resource.getProperties()==null ? new HashMap<String,String>() :
			ResourceUtils.filterResourceProperties(triggerRepository.getPropertyTemplates(null), 
					resource.getProperties()));

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.getRepository().setValues(resource, properties);

		for (TriggerResource child : resource.getChildTriggers()) {
			child.setParentTrigger(resource);
			performImport(child, realm);
		}
	}

	protected void prepareExport(AutomationResource resource) throws ResourceException, AccessDeniedException {

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		resource.setProperties(provider.getTaskProperties(resource));

		for (TriggerResource childTrigger : resource.getChildTriggers()) {
			prepareExport(childTrigger);
		}

		super.prepareExport(resource);
	}

	protected void prepareExport(TriggerResource resource) throws ResourceException, AccessDeniedException {

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		resource.setProperties(provider.getTaskProperties(resource));

		for (TriggerCondition condition : resource.getConditions()) {
			condition.setId(null);
			condition.setTrigger(null);
		}
		for (TriggerResource childTrigger : resource.getChildTriggers()) {
			prepareExport(childTrigger);
		}

		if (resource.getParentTrigger() != null) {
			resource.getParentTrigger().setId(null);
		}

		resource.setId(null);
	}
	
	protected void performImportDropResources(AutomationResource resource)
			throws ResourceException, AccessDeniedException {
		deleteResource(resource);
	}

	@Override
	public boolean isEnabled() {
		return Objects.isNull(controller) || controller.canAutomate();
	}
}
