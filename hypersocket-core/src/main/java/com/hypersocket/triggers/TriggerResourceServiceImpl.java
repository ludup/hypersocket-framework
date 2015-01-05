package com.hypersocket.triggers;

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

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
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
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.events.TriggerExecutedEvent;
import com.hypersocket.triggers.events.TriggerResourceCreatedEvent;
import com.hypersocket.triggers.events.TriggerResourceDeletedEvent;
import com.hypersocket.triggers.events.TriggerResourceUpdatedEvent;

@Service
public class TriggerResourceServiceImpl extends
		AbstractResourceServiceImpl<TriggerResource> implements
		TriggerResourceService, ApplicationListener<SystemEvent> {

	static Logger log = LoggerFactory
			.getLogger(TriggerResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "TriggerResourceService";

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
	
	
	Map<String, TriggerConditionProvider> registeredConditions = new HashMap<String, TriggerConditionProvider>();

	Map<String, ReplacementVariableProvider> replacementVariables = new HashMap<String, ReplacementVariableProvider>();

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
	public List<EventDefinition> getTriggerEvents() {
		List<EventDefinition> ret = new ArrayList<EventDefinition>();
		for (EventDefinition def : eventService.getEvents()) {
			ret.add(new EventDefinition(def, getEventAttributes(def)));
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
	public TriggerResource updateResource(TriggerResource resource,
			String name, String event, TriggerResultType result,
			Map<String, String> properties,
			List<TriggerCondition> anyConditions,
			List<TriggerCondition> allConditions, List<TriggerAction> actions)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);
		resource.getConditions().clear();

		populateTrigger(name, event, result, resource.getRealm(), resource,
				allConditions, anyConditions, actions);

		updateResource(resource, properties);

		for (TriggerAction action : resource.getActions()) {
			TaskProvider provider = taskService.getActionProvider(action
					.getResourceKey());
			provider.taskUpdated(action);
		}

		return resource;
	}

	@Override
	public TriggerResource createResource(String name, String event,
			TriggerResultType result, Map<String, String> properties,
			Realm realm, List<TriggerCondition> anyConditions,
			List<TriggerCondition> allConditions, List<TriggerAction> actions)
			throws ResourceCreationException, AccessDeniedException {

		TriggerResource resource = new TriggerResource();
		
		populateTrigger(name, event, result, realm, resource, allConditions,
				anyConditions, actions);

		createResource(resource, properties);

		for (TriggerAction action : resource.getActions()) {
			TaskProvider provider = taskService.getActionProvider(action
					.getResourceKey());
			provider.taskCreated(action);
		}

		return resource;
	}

	@Override
	public void deleteResource(TriggerResource resource)
			throws ResourceChangeException, AccessDeniedException {

		for (TriggerAction action : resource.getActions()) {
			TaskProvider provider = taskService.getActionProvider(action
					.getResourceKey());
			provider.taskDeleted(action);
		}

		super.deleteResource(resource);
	}

	private void populateTrigger(String name, String event,
			TriggerResultType result, Realm realm, TriggerResource resource,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, List<TriggerAction> actions) {

		resource.setName(name);
		resource.setEvent(event);
		resource.setResult(result);
		resource.setRealm(realm);
		resource.getConditions().clear();
		resource.getActions().clear();

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
		for (TriggerAction a : actions) {
			a.setTrigger(resource);
			resource.getActions().add(a);
		}

	}

	@Override
	public void registerConditionProvider(TriggerConditionProvider condition) {
		for (String resourceKey : condition.getResourceKeys()) {
			registeredConditions.put(resourceKey, condition);
		}
	}

	@Override
	public void onApplicationEvent(SystemEvent event) {
		try {
			processEventTriggers(event);
		} catch (Throwable t) {
			log.error("Failed to process triggers", t);
		}
	}

	private void processEventTriggers(SystemEvent event) {

		// TODO cache triggers to prevent constant database lookup

		if (log.isInfoEnabled()) {
			log.info("Looking for triggers for event " 
						+ event.getResourceKey() + " " 
						+ event.getStatus().toString());
		}

		List<TriggerResource> triggers = repository.getTriggersForEvent(event);
		for (TriggerResource trigger : triggers) {
			
			if(log.isInfoEnabled()) {
				log.info("Found trigger " + trigger.getName());
			}
			JobDataMap data = new JobDataMap();
			data.put("event", event);
			data.put("trigger", trigger);
			data.put("principal", getCurrentPrincipal());
			data.put("locale", getCurrentLocale());
			data.put("realm", getCurrentRealm());
			
			try {
				schedulerService.scheduleNow(ProcessEventTriggersJob.class,
						data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule event trigger job", e);
			}
		}

	}

	@Override
	public TriggerConditionProvider getConditionProvider(
			TriggerCondition condition) {
		return registeredConditions.get(condition.getConditionKey());
	}



	@Override
	public TriggerAction getActionById(Long id) throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return repository.getActionById(id);
	}

	@Override
	public TriggerCondition getConditionById(Long id)
			throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return repository.getConditionById(id);
	}

	@Override
	public Collection<TriggerAction> getActionsByResourceKey(String resourceKey) {

		return repository.getActionsByResourceKey(resourceKey);
	}

}
