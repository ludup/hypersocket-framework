package com.hypersocket.triggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.automation.AutomationResourceService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.http.HttpUtilsImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.SessionService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.events.TriggerExecutedEvent;
import com.hypersocket.triggers.events.TriggerResourceCreatedEvent;
import com.hypersocket.triggers.events.TriggerResourceDeletedEvent;
import com.hypersocket.triggers.events.TriggerResourceEvent;
import com.hypersocket.triggers.events.TriggerResourceUpdatedEvent;

@Service
public class TriggerResourceServiceImpl extends
		AbstractResourceServiceImpl<TriggerResource> implements
		TriggerResourceService, ApplicationListener<SystemEvent> {

	static Logger log = LoggerFactory
			.getLogger(TriggerResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "TriggerResourceService";

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";

	@Autowired
	TriggerResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	RealmService realmService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	ConfigurationService configurationService; 
	
	@Autowired
	AutomationResourceService automationService;
	
	@Autowired
	TriggerExecutor triggerExecutor;
	
	@Autowired
	SessionService sessionService; 
	
	@Autowired
	HttpUtilsImpl httpUtils;
	
	Map<String, TriggerConditionProvider> registeredConditions = new HashMap<String, TriggerConditionProvider>();

	Map<String, ReplacementVariableProvider> replacementVariables = new HashMap<String, ReplacementVariableProvider>();

	boolean running = true;

	public TriggerResourceServiceImpl() {
		super("triggerResource");
	}

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.triggers");

		for (TriggerResourcePermission p : TriggerResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);

		repository.loadPropertyTemplates("triggerTemplate.xml");

		/**
		 * Register the events. All events have to be registered so the system
		 * knows about them.
		 */
		eventService.registerEvent(TriggerResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(TriggerResourceCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(TriggerResourceUpdatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(TriggerResourceDeletedEvent.class,
				RESOURCE_BUNDLE, this);

		eventService.registerEvent(TriggerExecutedEvent.class, RESOURCE_BUNDLE);

		replacementVariables.put("currentUser.email",
				new ReplacementVariableProvider() {
					@Override
					public String getReplacementValue(String variable) {
						try {
							return realmService.getPrincipalAddress(
									getCurrentPrincipal(), MediaType.EMAIL);
						} catch (MediaNotFoundException e) {
							return "";
						}
					}
				});
		replacementVariables.put("currentUser.phone",
				new ReplacementVariableProvider() {
					@Override
					public String getReplacementValue(String variable) {
						try {
							return realmService.getPrincipalAddress(
									getCurrentPrincipal(), MediaType.PHONE);
						} catch (MediaNotFoundException e) {
							return "";
						}
					}
				});
	}

	@Override
	public Set<String> getDefaultVariableNames() {
		return new HashSet<String>(replacementVariables.keySet());
	}

	@Override
	public String getDefaultVariableValue(String variableName) {
		return replacementVariables.get(variableName).getReplacementValue(
				variableName);
	}

	@Override
	protected boolean checkUnique(TriggerResource resource, boolean create) throws AccessDeniedException {
		if(super.checkUnique(resource, create)) {
			try {
				automationService.getResourceByName(resource.getName());
				return false;
			} catch (ResourceNotFoundException e) {
				return true;
			} 
		}
		return false;
	}
	
	@Override
	public List<EventDefinition> getTriggerEvents() {
		List<EventDefinition> ret = new ArrayList<EventDefinition>();
		for (EventDefinition def : eventService.getEvents()) {
			ret.add(new EventDefinition(def, def.getI18nNamespace(),
					getEventAttributes(def)));
		}
		Collections.sort(ret, new Comparator<EventDefinition>() {

			@Override
			public int compare(EventDefinition o1, EventDefinition o2) {
				return o2.getResourceKey().compareTo(o1.getResourceKey());
			}
		});
		return ret;
	}

	@Override
	public Collection<String> getEventAttributes(String resourceKey) {
		return getEventAttributes(eventService.getEventDefinition(resourceKey));
	}

	private Set<String> getEventAttributes(EventDefinition evt) {

		Set<String> attributeNames = new HashSet<String>();
		attributeNames.addAll(evt.getAttributeNames());
		if (evt.getPropertyCollector() != null) {
			attributeNames.addAll(evt.getPropertyCollector().getPropertyNames(
					evt.getResourceKey(), getCurrentRealm()));
		}
		attributeNames.addAll(replacementVariables.keySet());
		return attributeNames;
	}

	@Override
	protected AbstractResourceRepository<TriggerResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public List<String> getConditions() {
		return new ArrayList<String>(registeredConditions.keySet());
	}

	@Override
	public Class<TriggerResourcePermission> getPermissionType() {
		return TriggerResourcePermission.class;
	}

	protected Class<TriggerResource> getResourceClass() {
		return TriggerResource.class;
	}

	@Override
	protected void fireResourceCreationEvent(TriggerResource resource) {
		eventService.publishEvent(new TriggerResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(TriggerResource resource,
			Throwable t) {
		eventService.publishEvent(new TriggerResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(TriggerResource resource) {
		eventService.publishEvent(new TriggerResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(TriggerResource resource, Throwable t) {
		eventService.publishEvent(new TriggerResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(TriggerResource resource) {
		eventService.publishEvent(new TriggerResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(TriggerResource resource,
			Throwable t) {
		eventService.publishEvent(new TriggerResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public TriggerResource updateResource(
			TriggerResource resource,
			String name,
			TriggerType type,
			String event,
			TriggerResultType result,
			String task,
			Map<String, String> properties,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions,
			TriggerResource parent,
			Long attachment,
			boolean allRealms,
			@SuppressWarnings("unchecked") TransactionAdapter<TriggerResource>... ops)
			throws ResourceChangeException, AccessDeniedException {

		resource.getConditions().clear();

		populateTrigger(name, event, result, task, resource.getRealm(),
				resource, allConditions, anyConditions, parent, attachment,
				allRealms);

		updateResource(resource, properties, ArrayUtils.add(ops, 0,
				new TransactionAdapter<TriggerResource>() {

					@Override
					public void afterOperation(TriggerResource resource,
							Map<String, String> properties) {
						TaskProvider provider = taskService
								.getTaskProvider(resource.getResourceKey());
						provider.getRepository()
								.setValues(resource, properties);
					}

				}));

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.taskUpdated(resource);

		return resource;
	}

	@Override
	public TriggerResource createResource(
			String name,
			TriggerType type,
			String event,
			TriggerResultType result,
			String task,
			Map<String, String> properties,
			Realm realm,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions,
			TriggerResource parent,
			Long attachment,
			boolean allRealms,
			@SuppressWarnings("unchecked") TransactionAdapter<TriggerResource>... ops)
			throws ResourceCreationException, AccessDeniedException {

		TriggerResource resource = new TriggerResource();

		resource.setTriggerType(type);

		populateTrigger(name, event, result, task, realm, resource,
				allConditions, anyConditions, parent, attachment, allRealms);

		createResource(resource, properties, ArrayUtils.add(ops, 0,
				new TransactionAdapter<TriggerResource>() {

					@Override
					public void afterOperation(TriggerResource resource,
							Map<String, String> properties) {

						TaskProvider provider = taskService
								.getTaskProvider(resource.getResourceKey());
						provider.getRepository()
								.setValues(resource, properties);

					}

				}));

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.taskCreated(resource);

		return resource;
	}

	@Override
	public void deleteResource(final TriggerResource resource)
			throws ResourceChangeException, AccessDeniedException {

		super.deleteResource(resource,
				new TransactionAdapter<TriggerResource>() {

					public void beforeOperation(TriggerResource resource,
							Map<String, String> properties) {

						try {
							for (TriggerResource child : resource
									.getChildTriggers()) {
								deleteResource(child);
							}
							resource.setParentTrigger(null);
							getRepository().deletePropertiesForResource(
									resource);
						} catch (Throwable e) {
							throw new IllegalStateException(e);
						}
					}
				});

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.taskDeleted(resource);
	}

	private void populateTrigger(String name, String event,
			TriggerResultType result, String task, Realm realm,
			TriggerResource resource, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent,
			Long attachment, boolean allRealms) {

		resource.setName(name);
		resource.setEvent(event);
		resource.setResult(result);
		resource.setRealm(realm);
		resource.setResourceKey(task);
		resource.setParentTrigger(parent);
		resource.getConditions().clear();
		resource.setAttachmentId(attachment);
		resource.setAllRealms(allRealms);

		for (TriggerCondition c : allConditions) {
			c.setType(TriggerConditionType.ALL);
			c.setTrigger(resource);
			resource.getConditions().add(c);
		}
		for (TriggerCondition c : anyConditions) {
			c.setType(TriggerConditionType.ANY);
			c.setTrigger(resource);
			resource.getConditions().add(c);
		}

	}

	@Override
	public void registerConditionProvider(TriggerConditionProvider condition) {
		for (String resourceKey : condition.getResourceKeys()) {
			registeredConditions.put(resourceKey, condition);
		}
	}

	@Override
	public void onApplicationEvent(final SystemEvent event) {
		sessionService.executeInSystemContext(new Runnable() {

			@Override
			public void run() {
				try {
					processEventTriggers(event);
				} catch (Throwable t) {
					log.error("Failed to process triggers", t);
				}
			}
			
		});
		
	}

	private void processEventTriggers(SystemEvent sourceEvent) {

		// TODO cache triggers to prevent constant database lookup

		// TODO need some security to prevent inifinte loops
		if (!running) {
			if (log.isDebugEnabled()) {
				log.debug("Not processing triggers as the service is not running");
			}
			return;
		}

		if (log.isInfoEnabled()) {
			log.info("Looking for triggers for events "
					+ StringUtils.join(sourceEvent.getResourceKeys(), ",") + " "
					+ sourceEvent.getStatus().toString());
		}

		List<TriggerResource> triggers = repository.getTriggersForEvent(sourceEvent);
		
		for (TriggerResource trigger : triggers) {

			if (log.isInfoEnabled()) {
				log.info("Found trigger " + trigger.getName());
			}
			
			try {
				triggerExecutor.scheduleOrExecuteTrigger(trigger, sourceEvent);
			} catch (ValidationException e) {
				log.error("Trigger execution failed", e);
			}
			
		}

	}

	@Override
	public TriggerConditionProvider getConditionProvider(
			TriggerCondition condition) {
		return registeredConditions.get(condition.getConditionKey());
	}

	@Override
	public TriggerCondition getConditionById(Long id)
			throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return repository.getConditionById(id);
	}

	@Override
	public Collection<TriggerResource> getTriggersByResourceKey(
			String resourceKey) {

		return repository.getActionsByResourceKey(resourceKey);
	}

	@Override
	public Collection<String> getTasks() throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return taskService.getTriggerTasks();
	}

	@Override
	public List<TriggerResource> getParentTriggers(Long id)
			throws ResourceNotFoundException, AccessDeniedException {

		List<TriggerResource> triggers = new ArrayList<TriggerResource>();

		TriggerResource trigger = getResourceById(id);
		triggers.add(trigger);
		while (trigger != null && trigger.getParentTrigger() != null) {
			triggers.add(trigger.getParentTrigger());
			trigger = trigger.getParentTrigger();
		}

		return triggers;
	}

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		running = false;
	}

	protected boolean isExportingAdditionalProperties() {
		return false;
	}

	protected void performImport(TriggerResource resource, Realm realm)
			throws ResourceException, AccessDeniedException {

		Map<String, String> properties = resource.getProperties();
		resource.setProperties(null);

		super.performImport(resource, realm);

		TaskProvider provider = taskService.getTaskProvider(resource
				.getResourceKey());
		provider.getRepository().setValues(resource, properties);

		for (TriggerResource child : resource.getChildTriggers()) {
			child.setParentTrigger(resource);
			performImport(child, realm);
		}
	}

	protected void prepareExport(TriggerResource resource) {

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

		super.prepareExport(resource);
	}

	protected void performImportDropResources(TriggerResource resource)
			throws ResourceChangeException, AccessDeniedException {
		deleteResource(resource);
	}

	@Override
	public void downloadTemplateImage(String uuid, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		request.setAttribute(
				CONTENT_INPUTSTREAM,
				httpUtils.doHttpGet(
						System.getProperty(
								"hypersocket.templateServerImageUrl",
								"https://templates1x.hypersocket.com/hypersocket/api/templates/image/")
								+ uuid, true));

	}

	@Override
	public String searchTemplates(String search, int iDisplayStart,
			int iDisplayLength) throws IOException, AccessDeniedException {
		assertPermission(TriggerResourcePermission.CREATE);

		Map<String, String> params = new HashMap<String, String>();
		params.put("sSearch", search);
		params.put("iDisplayStart", String.valueOf(iDisplayStart));
		params.put("iDisplayLength", String.valueOf(iDisplayLength));
		params.put("sEcho", "0");
		params.put("iSortingCols", "1");
		params.put("iSortCol_0", "0");
		params.put("sSortDir_0", "asc");

		String json = httpUtils
				.doHttpPost(
						System.getProperty("hypersocket.templateServerUrl",
								"https://templates1x.hypersocket.com/hypersocket/api/templates")
								+ "/"
								+ (Boolean
										.getBoolean("hypersocketTriggers.enablePrivate") ? "developer"
										: "table" + "/3"), params, true);

		return json;
	}

}
