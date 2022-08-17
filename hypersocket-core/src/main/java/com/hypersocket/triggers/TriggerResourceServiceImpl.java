package com.hypersocket.triggers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.automation.AutomationResourceService;
import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.http.HttpUtilsImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.session.SessionService;
import com.hypersocket.tasks.TaskDefinition;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.events.TriggerExecutedEvent;
import com.hypersocket.triggers.events.TriggerResourceCreatedEvent;
import com.hypersocket.triggers.events.TriggerResourceDeletedEvent;
import com.hypersocket.triggers.events.TriggerResourceEvent;
import com.hypersocket.triggers.events.TriggerResourceUpdatedEvent;
import com.hypersocket.util.Iterators;
import com.hypersocket.util.MaxSizeHashMap;

@Service
public class TriggerResourceServiceImpl extends AbstractResourceServiceImpl<TriggerResource>
		implements TriggerResourceService, ApplicationListener<SystemEvent> {

	static Logger log = LoggerFactory.getLogger(TriggerResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "TriggerResourceService";

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";

	private static final int MAX_CACHED_TRIGGERS = 1024;

	@Autowired
	private TriggerResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private AutomationResourceService automationService;

	@Autowired
	private TriggerExecutor triggerExecutor;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private HttpUtilsImpl httpUtils;

	private Map<String, TriggerConditionProvider> registeredConditions = new HashMap<String, TriggerConditionProvider>();
	private Map<String, ReplacementVariableProvider> replacementVariables = new HashMap<String, ReplacementVariableProvider>();
	private MaxSizeHashMap<String, List<TriggerResource>> eventTriggerCache;
	private TriggerController controller;
	private boolean running = true;

	public TriggerResourceServiceImpl() {
		super("triggerResource");
	}

	@PostConstruct
	private void postConstruct() {

		eventTriggerCache = new MaxSizeHashMap<>(MAX_CACHED_TRIGGERS);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.triggers");

		for (TriggerResourcePermission p : TriggerResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);

		repository.loadPropertyTemplates("triggerTemplate.xml", getClass().getClassLoader());

		/**
		 * Register the events. All events have to be registered so the system knows
		 * about them.
		 */
		eventService.registerEvent(TriggerResourceEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(TriggerResourceCreatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(TriggerResourceUpdatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(TriggerResourceDeletedEvent.class, RESOURCE_BUNDLE, this);

		eventService.registerEvent(TriggerExecutedEvent.class, RESOURCE_BUNDLE);

		new DefaultVariableReplacementProvider("default.timestamp") {

			@Override
			public String getReplacementValue(String variable) {
				return String.valueOf(System.currentTimeMillis());
			}

		};
		
		new DefaultVariableReplacementProvider("default.currentUser.email") {

			@Override
			public String getReplacementValue(String variable) {
				try {
					return realmService.getPrincipalAddress(getCurrentPrincipal(), MediaType.EMAIL);
				} catch (MediaNotFoundException e) {
					return "";
				}
			}

		};

		new DefaultVariableReplacementProvider("default.currentUser.phone") {

			@Override
			public String getReplacementValue(String variable) {
				try {
					return realmService.getPrincipalAddress(getCurrentPrincipal(), MediaType.PHONE);
				} catch (MediaNotFoundException e) {
					return "";
				}
			}

		};

		new DefaultVariableReplacementProvider("default.administrators.email") {

			@Override
			public String getReplacementValue(String variable) {
				StringBuffer buf = new StringBuffer();
				try {
					for (Principal principal : Iterators.iterable(permissionService.iteratePrincipalsByRole(getCurrentRealm(),
							permissionService.getRealmAdministratorRole(getCurrentRealm())))) {
						if (StringUtils.isNotBlank(principal.getPrimaryEmail())) {
							if (buf.length() > 0) {
								buf.append("\r\n");
							}
							buf.append(principal.getPrimaryEmail());
						}
					}
				} catch (ResourceNotFoundException | AccessDeniedException e) {
					log.error("Failed to lookup administrators emails", e);
				}
				return buf.toString();
			}

		};
	}

	@Override
	public void setController(TriggerController controller) {
		this.controller = controller;
	}
	
	@Override
	public void registerReplacementVariables(ReplacementVariableProvider provider) {
		for (String variable : provider.getReplacementVariableNames()) {
			replacementVariables.put(variable, provider);
		}
	}

	@Override
	public Set<String> getDefaultVariableNames() {
		Set<String> tmp = new HashSet<String>();
		for (ReplacementVariableProvider provider : replacementVariables.values()) {
			tmp.addAll(provider.getReplacementVariableNames());
		}
		return tmp;
	}

	@Override
	public String getDefaultVariableValue(String variableName) {
		/*
		 * Backwards compatibiltiy
		 */
		if (variableName.startsWith("currentUser.")) {
			variableName = "default." + variableName;
		}
		if (variableName.equals("administrators.email")) {
			variableName = "default." + variableName;
		}
		return replacementVariables.get(variableName).getReplacementValue(variableName);
	}

	@Override
	protected boolean checkUnique(TriggerResource resource, boolean create) throws AccessDeniedException {
		if (super.checkUnique(resource, create)) {
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
			ret.add(new EventDefinition(def, def.getI18nNamespace(), getEventAttributes(def)));
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
			attributeNames.addAll(evt.getPropertyCollector().getPropertyNames(evt.getResourceKey(), getCurrentRealm()));
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
		eventService.publishEvent(new TriggerResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(TriggerResource resource, Throwable t) {
		eventService.publishEvent(new TriggerResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(TriggerResource resource) {
		eventService.publishEvent(new TriggerResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(TriggerResource resource, Throwable t) {
		eventService.publishEvent(new TriggerResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(TriggerResource resource) {
		eventService.publishEvent(new TriggerResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(TriggerResource resource, Throwable t) {
		eventService.publishEvent(new TriggerResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public TriggerResource updateResource(TriggerResource resource, String name, TriggerType type, String event,
			TriggerResultType result, String task, Map<String, String> properties, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent, Long attachment, boolean allRealms,
			@SuppressWarnings("unchecked") TransactionAdapter<TriggerResource>... ops)
			throws ResourceException, AccessDeniedException {

		resource.getConditions().clear();

		populateTrigger(name, event, result, task, resource.getRealm(), resource, allConditions, anyConditions, parent,
				attachment, allRealms);

		updateResource(resource, properties, ArrayUtils.insert(0, ops, new TransactionAdapter<TriggerResource>() {

			@Override
			public void afterOperation(TriggerResource resource, Map<String, String> properties) {
				TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
				provider.getRepository().setValues(resource, properties);
			}

		}));

		TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
		provider.taskUpdated(resource);
		eventTriggerCache.clear();

		return resource;
	}

	@Override
	public TriggerResource createResource(String name, TriggerType type, String event, TriggerResultType result,
			String task, Map<String, String> properties, Realm realm, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent, Long attachment, boolean allRealms,
			@SuppressWarnings("unchecked") TransactionAdapter<TriggerResource>... ops)
			throws ResourceException, AccessDeniedException {

		TriggerResource resource = new TriggerResource();

		resource.setTriggerType(type);

		populateTrigger(name, event, result, task, realm, resource, allConditions, anyConditions, parent, attachment,
				allRealms);

		createResource(resource, properties, ArrayUtils.insert(0, ops, new TransactionAdapter<TriggerResource>() {

			@Override
			public void afterOperation(TriggerResource resource, Map<String, String> properties) {

				TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
				provider.getRepository().setValues(resource, properties);

			}

		}));

		TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
		provider.taskCreated(resource);
		eventTriggerCache.clear();

		return resource;
	}

	@Override
	public void deleteResource(final TriggerResource resource) throws ResourceException, AccessDeniedException {

		super.deleteResource(resource, new TransactionAdapter<TriggerResource>() {

			@SuppressWarnings("unchecked")
			public void beforeOperation(TriggerResource resource, Map<String, String> properties) {

				try {
					for (TriggerResource child : resource.getChildTriggers()) {
						getRepository().deletePropertiesForResource(child);
						getRepository().deleteResource(child);
					}
					getRepository().deletePropertiesForResource(resource);
					getRepository().deleteResource(resource);;
				} catch (Throwable e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
		});

		TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
		provider.taskDeleted(resource);
		eventTriggerCache.clear();
	}

	private void populateTrigger(String name, String event, TriggerResultType result, String task, Realm realm,
			TriggerResource resource, List<TriggerCondition> allConditions, List<TriggerCondition> anyConditions,
			TriggerResource parent, Long attachment, boolean allRealms) {

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
		sessionService.runAsSystemContext(() -> {
			try {
				processEventTriggers(event);
			} catch (Throwable t) {
				log.error("Failed to process triggers", t);
			}
		});

	}

	private void processEventTriggers(SystemEvent sourceEvent) {

		// TODO need some security to prevent inifinte loops
		if (!running) {
			if (log.isDebugEnabled()) {
				log.debug("Not processing triggers as the service is not running");
			}
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Looking for triggers for events " + StringUtils.join(sourceEvent.getResourceKeys(), ",") + " "
					+ sourceEvent.getStatus().toString());
		}

		String cacheKey = getCacheKey(sourceEvent);
		List<TriggerResource> triggers = eventTriggerCache.get(cacheKey);
		if (triggers == null) {
			triggers = repository.getTriggersForEvent(sourceEvent);
			eventTriggerCache.put(cacheKey, triggers);
		}

		for (TriggerResource trigger : triggers) {

			if (log.isDebugEnabled()) {
				log.debug("Found trigger " + trigger.getName());
			}

			try {
				List<SystemEvent> sourceEvents = new ArrayList<SystemEvent>();
				sourceEvents.add(sourceEvent);
				triggerExecutor.scheduleOrExecuteTrigger(trigger, sourceEvents);
			} catch (ValidationException e) {
				log.error("Trigger execution failed", e);
			}

		}

	}

	@Override
	public TriggerConditionProvider getConditionProvider(TriggerCondition condition) {
		return registeredConditions.get(condition.getConditionKey());
	}

	@Override
	public TriggerCondition getConditionById(Long id) throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return repository.getConditionById(id);
	}

	@Override
	public Collection<TriggerResource> getTriggersByTask(String resourceKey) {

		return repository.getTriggersByTask(resourceKey);
	}

	@Override
	public Collection<TriggerResource> getTriggersByEvent(String resourceKey) {

		return repository.getTriggersByEvent(resourceKey);
	}

	
	@Override
	public Collection<TaskDefinition> getTasks() throws AccessDeniedException {

		assertPermission(TriggerResourcePermission.READ);

		return taskService.getTriggerTasks();
	}

	@Override
	public List<TriggerResource> getParentTriggers(Long id) throws ResourceNotFoundException, AccessDeniedException {

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

		TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
		provider.getRepository().setValues(resource, properties);

		for (TriggerResource child : resource.getChildTriggers()) {
			child.setParentTrigger(resource);
			performImport(child, realm);
		}
	}

	protected void prepareExport(TriggerResource resource) throws ResourceException, AccessDeniedException {

		TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
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
			throws ResourceException, AccessDeniedException {
		deleteResource(resource);
	}

	@Override
	public void downloadTemplateImage(String uuid, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		String uri = System.getProperty("hypersocket.templateServerImageUrl",
				"https://updates2.hypersocket.com/hypersocket/api/templates/image/") + uuid;
		CloseableHttpResponse resp = httpUtils.doHttpGet(uri, true, null);
		if(resp.getStatusLine().getStatusCode() != 200) {
			response.setStatus(resp.getStatusLine().getStatusCode());
			return;
		}
		response.setContentType(resp.getFirstHeader("Content-Type").getValue());
		Header clength = resp.getFirstHeader("Content-Length");
		if(clength != null)
			response.setContentLength(Integer.parseInt(clength.getValue()));
		Header lastmod = resp.getFirstHeader("Last-Modified");
		if(lastmod != null)
			response.setHeader("Last-Modified", lastmod.getValue());
		try(InputStream in = resp.getEntity().getContent()) {
			IOUtils.copy(in, response.getOutputStream());
		}
	}

	@Override
	public String searchTemplates(String search, int iDisplayStart, int iDisplayLength)
			throws IOException, AccessDeniedException {
		assertPermission(TriggerResourcePermission.CREATE);

		Map<String, String> params = new HashMap<String, String>();
		params.put("sSearch", search);
		params.put("iDisplayStart", String.valueOf(iDisplayStart));
		params.put("iDisplayLength", String.valueOf(iDisplayLength));
		params.put("sEcho", "0");
		params.put("iSortingCols", "1");
		params.put("iSortCol_0", "0");
		params.put("sSortDir_0", "asc");

		String url = System.getProperty("hypersocket.templateServerUrl",
				"https://updates2.hypersocket.com/hypersocket/api/templates") + "/"
				+( (Boolean.getBoolean("hypersocketTriggers.enablePrivate") ? "developer" : "table" ) + "/3");
		log.info(String.format("Retrieving from %s", url));
		String json = httpUtils.doHttpPost(
				url,
				params, true);

		return json;
	}

	@Override
	public List<EventDefinition> getTriggerEvents(String pattern, Locale locale) {

		List<EventDefinition> ret = new ArrayList<EventDefinition>();
		String[] cards = pattern.toLowerCase().split("\\*");
		for (EventDefinition def : eventService.getEvents()) {
			boolean matchesPattern = true;
			String eventName = I18N.getResource(locale, def.getResourceBundle(), def.getResourceKey()).toLowerCase();
			for (String card : cards) {
				int idx = eventName.indexOf(card);
				if (idx == -1) {
					matchesPattern = false;
					break;
				}
				if (eventName.length() < idx + card.length()) {
					eventName = "";
				} else {
					eventName = eventName.substring(idx + card.length());
				}
			}
			if (matchesPattern) {
				ret.add(new EventDefinition(def, def.getI18nNamespace(), getEventAttributes(def)));
			}
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
	public Long getTriggerEventCount(String pattern, Locale locale) {
		List<EventDefinition> ret = new ArrayList<EventDefinition>();
		for (EventDefinition def : eventService.getEvents()) {
			if (I18N.getResource(locale, def.getResourceBundle(), def.getResourceKey()).toLowerCase()
					.indexOf(pattern.toLowerCase()) > -1) {
				ret.add(new EventDefinition(def, def.getI18nNamespace(), getEventAttributes(def)));
			}
		}
		Collections.sort(ret, new Comparator<EventDefinition>() {

			@Override
			public int compare(EventDefinition o1, EventDefinition o2) {
				return o2.getResourceKey().compareTo(o1.getResourceKey());
			}
		});
		return Long.valueOf(ret.size());
	}

	private String getCacheKey(SystemEvent sourceEvent) {
		return sourceEvent.getCurrentRealm().getId() + "-"
				+ String.join("-", sourceEvent.getResourceKeys() + "-" + sourceEvent.getStatus().name());
	}

	abstract class DefaultVariableReplacementProvider implements ReplacementVariableProvider {

		DefaultVariableReplacementProvider(String variable) {
			replacementVariables.put(variable, this);
		}

		@Override
		public Set<String> getReplacementVariableNames() {
			return replacementVariables.keySet();
		}
	}
	
	@Override
	public boolean isEnabled() {
		return Objects.isNull(controller) || controller.canTrigger();
	}
}
