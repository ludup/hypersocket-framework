package com.hypersocket.automation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
import com.hypersocket.events.SystemEventStatus;
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
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.SessionService;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResultType;
import com.hypersocket.triggers.TriggerType;
import com.hypersocket.utils.HypersocketUtils;

import antlr.StringUtils;

@Service
public class AutomationResourceServiceImpl extends AbstractResourceServiceImpl<AutomationResource>
		implements AutomationResourceService, ApplicationListener<ContextStartedEvent> {

	private static Logger log = LoggerFactory.getLogger(AutomationResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "AutomationResourceService";
	
	private static final String TEXT_IP = "text.ip";
	private static final String TEXT_SUMMARY = "text.summary";
	private static final String TEXT_TIMESTAMP = "text.timestamp";
	private static final String TEXT_REALM = "text.realm";
	private static final String TEXT_STATUS = "text.status";
	private static final String TEXT_EVENT = "text.event";

	@Autowired
	AutomationResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	SessionService sessionService;
	
	@Autowired
	ClusteredSchedulerService schedulerService;
	
	@Autowired
	SchedulingResourceService resourceScheduler;
	
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
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(AutomationResourceEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceCreatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceUpdatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationResourceDeletedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(AutomationTaskStartedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(AutomationTaskFinishedEvent.class, RESOURCE_BUNDLE);
		EntityResourcePropertyStore.registerResourceService(AutomationResource.class, repository);
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
	public Collection<String> getTasks() throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		return taskService.getAutomationTasks();
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {

		if (log.isInfoEnabled()) {
			log.info("Scheduling one time only or repetitive automation resources");
		}

		sessionService.executeInSystemContext(new Runnable() {

			@Override
			public void run() {
			
				for (Realm realm : realmRepository.allRealms()) {
					for (AutomationResource resource : repository.getResources(realm)) {
						if (!resource.isDailyJob()) {
							schedule(resource);
						}
					}
				}
		
				scheduleDailyJobs();
			}
			
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
	
	public List<?> getCsvAutomations(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting){
		return repository.getCsvAutomations(realm, searchColumn, searchPattern, start, length, sorting);
	}

	public Long getCsvAutomationsCount(Realm realm, String searchColumn, String searchPattern){
		return repository.getCsvAutomationsCount(realm, searchColumn, searchPattern);
	}
	
//	@Override
//	public void generateChart(AutomationResource resource, HttpServletResponse response, Locale locale) throws AccessDeniedException{
//		System.out.println("asd");
//		Collection<PropertyCategory> automationProperties = getPropertyTemplate(resource);
//		for (PropertyCategory t : automationProperties) {
//			if (properties.containsKey(t.getResourceKey())) {
//				if (t.getDisplayMode() == null || !t.getDisplayMode().equals("admin")) {
//					changedProperties.put(t.getResourceKey(), properties.get(t.getResourceKey()));
//				}
//			}
//		}
//		resource.getProperties();
//		resource.
//		resource.
//	}
//	@Override
//	public void generateChart(Realm realm, AuditFilterResource filter, Date startDate, Date endDate, String filename,
//			boolean outputHeaders, String delimiter, CommonEndOfLineEnum terminate, String wrap, String escape,
//			String attributes, ColumnSort[] sort, OutputStream out, Locale locale)
//			throws AccessDeniedException, UnsupportedEncodingException {
//		assertPermission(AuditLogPermission.READ);

//		List<Map<String, String>> fileRows = generateCSVRows(realm, filter, startDate, endDate, attributes, sort, locale);
//		List<String> headers = new ArrayList<String>();
//		headers.add(TEXT_EVENT);
//		headers.add(TEXT_STATUS);
//		headers.add(TEXT_REALM);
//		headers.add(TEXT_TIMESTAMP);
//		headers.add(TEXT_IP);
//		headers.add(TEXT_SUMMARY);
//		Cache<String, String> i18n = i18nService.getResourceMap(locale);
//		Set<String> includeAttributes = new HashSet<String>();
//		includeAttributes.addAll(Arrays.asList(attributes.split(",")));
//		for (String attributeName : includeAttributes) {
//			headers.add(attributeName);
//		}
//		ICsvMapWriter mapWriter = null;
//		Writer writer = new OutputStreamWriter(out, "UTF-8");
//		try {
//			String terminateCharacter = terminate.getCharacter();
//
//			final CsvPreference preferences = new CsvPreference.Builder(wrap.charAt(0), delimiter.charAt(0),
//					terminateCharacter).surroundingSpacesNeedQuotes(true).build();
//			mapWriter = new CsvMapWriter(writer, preferences);
//
//			final CellProcessor[] processors = getProcessors(headers.size());
//			if (outputHeaders) {
//
//				List<String> i18nHeaders = new ArrayList<String>();
//				for (String header : headers) {
//					String txt = i18n.get(header);
//					if (StringUtils.isNotBlank(txt)) {
//						i18nHeaders.add(txt);
//					} else {
//						i18nHeaders.add(header);
//					}
//
//				}
//				mapWriter.writeHeader(i18nHeaders.toArray(new String[0]));
//			}
//			for (Map<String, String> row : fileRows) {
//				mapWriter.write(row, headers.toArray(new String[0]), processors);
//			}
//			mapWriter.flush();
//		} catch (IOException e) {
//			log.error("Error generating CSV", e);
//			throw new IllegalStateException(e.getMessage(), e);
//		} finally {
//			IOUtils.closeQuietly(mapWriter);
//			IOUtils.closeQuietly(writer);
//		}
//
//	}
//
//	protected List<Map<String, String>> generateCSVRows(Realm realm, AuditFilterResource filter, Date startDate, Date endDate,
//			String attributes, ColumnSort[] sort, Locale locale) throws AccessDeniedException {
//
//		Collection<Realm> realms = new ArrayList<Realm>();
//		if(realm==null) {
//			realms.add(getCurrentRealm());
//			realms.addAll(realmService.getRealmsByParent(getCurrentRealm()));
//		} else {
//			realms.add(realm);
//		}
//		
//		
//		Collection<String> eventList = null;
//		Collection<SystemEventStatus> statusList = null;
//
//		if (filter.getIncludedEvents() != null && !"".equalsIgnoreCase(filter.getIncludedEvents())) {
//			eventList = new ArrayList<String>(Arrays.asList(filter.getIncludedEvents().split("\\]|\\[")));
//		}
//		if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
//			statusList = new ArrayList<SystemEventStatus>(filter.getStatuses());
//		}
//
//		List<AuditLog> auditLogList = auditRepository.search(eventList, statusList, 
//				startDate, endDate, sort, realms);
//
//		List<String> headers = new ArrayList<String>();
//		headers.add(TEXT_EVENT);
//		headers.add(TEXT_STATUS);
//		headers.add(TEXT_REALM);
//		headers.add(TEXT_TIMESTAMP);
//		headers.add(TEXT_IP);
//		headers.add(TEXT_SUMMARY);
//
//		Set<String> includeAttributes = new HashSet<String>();
//		includeAttributes.addAll(Arrays.asList(attributes.split("\\]\\|\\[")));
//
//		List<Map<String, String>> fileRows = new ArrayList<Map<String, String>>();
//
//		Cache<String, String> i18n = i18nService.getResourceMap(locale);
//		for (AuditLog auditLog : auditLogList) {
//			final Map<String, String> auditLogMap = new HashMap<String, String>();
//			auditLogMap.put(TEXT_EVENT, i18n.get(auditLog.getResourceKey()));
//			auditLogMap.put(TEXT_STATUS, auditLog.getStatus().toString());
//			auditLogMap.put(TEXT_REALM, auditLog.getRealm().getName());
//			auditLogMap.put(TEXT_TIMESTAMP,
//					HypersocketUtils.formatDate(new Date(auditLog.getTimestamp()), "yyyy-MM-dd HH:mm:ss"));
//
//			String summary = "";
//			if (SystemEventStatus.WARNING.equals(auditLog.getStatus())) {
//				summary = i18n.get(auditLog.getResourceKey() + ".warning");
//			} else if (SystemEventStatus.FAILURE.equals(auditLog.getStatus())) {
//				summary = i18n.get(auditLog.getResourceKey() + ".failure");
//			} else if (SystemEventStatus.SUCCESS.equals(auditLog.getStatus())) {
//				summary = i18n.get(auditLog.getResourceKey() + ".success");
//			}
//
//			Map<String, AuditLogAttribute> attributeMap = auditLog.getAttributes();
//			for (String attribute : attributeMap.keySet()) {
//				if (includeAttributes.contains(attribute)) {
//					headers.add(attribute);
//				}
//				auditLogMap.put(attribute, attributeMap.get(attribute).getValue());
//			}
//
//			if (StringUtils.isNotBlank(summary)) {
//				Matcher m = Pattern.compile("\\$?\\{(.*?)\\}").matcher(summary);
//				while (m.find()) {
//					String attrKey = m.group().replace('$', ' ').replace('{', ' ').replace('}', ' ').trim();
//					if (attributeMap.containsKey(attrKey)) {
//						String attributeValue = attributeMap.get(attrKey).getValue();
//						if (attributeValue.startsWith("i18n") && attributeValue.split("/").length == 3) {
//							String resourceKey = attributeValue.split("/")[2];
//							String value = i18n.get(resourceKey);
//							if (value == null) {
//								value = resourceKey;
//							}
//							summary = summary.replace(m.group(), value);
//						} else {
//							summary = summary.replace(m.group(), attributeValue);
//						}
//					}
//				}
//				auditLogMap.put(TEXT_SUMMARY, summary);
//			}
//
//			if (attributeMap.containsKey("attr.ipAddress")) {
//				auditLogMap.put(TEXT_IP, attributeMap.get("attr.ipAddress").getValue());
//			}
//
//			fileRows.add(auditLogMap);
//		}
//
//		return fileRows;
//	}
//
//	@Override
//	public AutomationResource getAutomationById(Long id, Realm currentRealm) {
//		return repository.getAutomationById(id, currentRealm);
//	}

}
