package com.hypersocket.triggers.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.AuthenticationRequiredButDontTouchSession;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceExportException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;
import com.hypersocket.tasks.TaskDefinition;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceColumns;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerResultType;
import com.hypersocket.utils.HypersocketUtils;


@Controller
public class TriggerResourceController extends AbstractTriggerController {

	@Autowired
	private TriggerResourceService resourceService;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@AuthenticationRequired
	@RequestMapping(value = "triggers/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableTriggers(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return TriggerResourceColumns.valueOf(col.toUpperCase());
					}

					@Override
					public List<?> getPage(String searchColumn, String searchPattern, int start,
							int length, ColumnSort[] sorting)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.searchResources(
								sessionUtils.getCurrentRealm(request),
								searchColumn, searchPattern, start, length, sorting);
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.getResourceCount(
								sessionUtils.getCurrentRealm(request),
								searchColumn, searchPattern);
					}
				});
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(
					resourceService.getResourceTemplate());
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceProperties(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		TriggerResource resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(taskService
				.getTaskProvider(resource.getResourceKey())
				.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/events", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<EventDefinition> getEvents(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceList<EventDefinition>(
				resourceService.getTriggerEvents());
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/event/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<EventDefinition> getEventDefinition(
			HttpServletRequest request, @PathVariable String resourceKey)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceStatus<EventDefinition>(
				eventService.getEventDefinition(resourceKey));
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/parentEvents/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<EventDefinition> getParentEventDefinitions(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {

		List<TriggerResource> triggers = resourceService.getParentTriggers(id);
		List<EventDefinition> events = new ArrayList<EventDefinition>();
		for (TriggerResource trigger : triggers) {
			if(trigger.getId()!=id) {
				EventDefinition evtDef = eventService.getEventDefinition(trigger.getEvent());
				if(evtDef == null)
					throw new IllegalStateException(String.format("No event definition for %s. It is possible the extension that provided this task is no longer installed.", trigger.getEvent()));
				events.add(evtDef);
			}
		}
		return new ResourceList<EventDefinition>(events);
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/taskResults/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<EventDefinition> getPostTriggerEvents(
			HttpServletRequest request, @PathVariable String resourceKey)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {

		List<EventDefinition> events = new ArrayList<EventDefinition>();

		if(eventService.isDynamicEvent(resourceKey)) {
			events.add(eventService.getEventDefinition(resourceKey));
		} else {
			final TaskProvider taskProvider = taskService.getTaskProvider(resourceKey);
			if(taskProvider == null)
				throw new ResourceNotFoundException(TriggerResourceServiceImpl.RESOURCE_BUNDLE, "error.notfound", resourceKey);
			events.add(eventService.getEventDefinition(taskProvider.getResultResourceKey()));
		}
		return new ResourceList<EventDefinition>(events);
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/eventAttributes/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<String> getEventAttributes(HttpServletRequest request,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<String>(
				resourceService.getEventAttributes(resourceKey));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "triggers/defaultAttributes", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<String> getEventAttributes(HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<String>(
				resourceService.getDefaultVariableNames());
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/tasks", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<TaskDefinition> getTasks(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		List<TaskDefinition> result = new ArrayList<>();
		for (TaskDefinition task : resourceService.getTasks()) {
			result.add(task);
		}
		return new ResourceList<>(result);
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/task/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getTaskTemplate(
			HttpServletRequest request, @PathVariable String resourceKey)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceList<PropertyCategory>(taskService
				.getTaskProvider(resourceKey).getPropertyTemplate(null));
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/conditions", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<String> getConditions(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceList<String>(resourceService.getConditions());
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/trigger/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public TriggerResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		return resourceService.getResourceById(id);

	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "triggers/trigger", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<TriggerResource> createOrUpdateTriggerResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody TriggerResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			TriggerResource newResource;

			Realm realm = sessionUtils.getCurrentRealm(request);

			List<TriggerCondition> allConditions = new ArrayList<TriggerCondition>();
			List<TriggerCondition> anyConditions = new ArrayList<TriggerCondition>();

			TriggerResource parentTrigger = null;
			if (resource.getParentId() != null && resource.getParentId() > 0) {
				parentTrigger = resourceService.getResourceById(resource
						.getParentId());
			}

			processConditions(resource, allConditions, anyConditions);

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			if (resource.getId() != null) {
				newResource = resourceService.updateResource(
						resourceService.getResourceById(resource.getId()),
						resource.getName(), resource.getType(),
						resource.getEvent(),
						TriggerResultType.valueOf(resource.getResult()),
						resource.getTask(), properties, allConditions,
						anyConditions, parentTrigger, null,
						resource.isAllRealms());
			} else {
				newResource = resourceService.createResource(
						resource.getName(), resource.getType(),
						resource.getEvent(),
						TriggerResultType.valueOf(resource.getResult()),
						resource.getTask(), properties, realm, allConditions,
						anyConditions, parentTrigger, null,
						resource.isAllRealms());
			}

			TriggerResource rootTrigger = newResource;

			while (rootTrigger.getParentTrigger() != null) {
				rootTrigger = rootTrigger.getParentTrigger();
			}

			return new ResourceStatus<TriggerResource>(rootTrigger,
					I18N.getResource(sessionUtils.getLocale(request),
							TriggerResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false, e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/trigger/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<TriggerResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			TriggerResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<TriggerResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();

			TriggerResource rootTrigger = null;

			if (resource.getParentTrigger() != null) {
				rootTrigger = resource;
				while (rootTrigger.getParentTrigger() != null) {
					rootTrigger = rootTrigger.getParentTrigger();
				}
			}

			resourceService.deleteResource(resource);

			if (rootTrigger != null) {
				return new ResourceStatus<TriggerResource>(
						resourceService.getResourceById(rootTrigger.getId()),
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"resource.deleted.info", preDeletedName));
			} else {
				return new ResourceStatus<TriggerResource>(true,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"resource.deleted.info", preDeletedName));
			}

		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false, e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/eventResults", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getLocales(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException {

		List<SelectOption> results = new ArrayList<SelectOption>();

		for (TriggerResultType t : TriggerResultType.values()) {
			results.add(new SelectOption(t.name(), t.name().toLowerCase()));
		}

		return new ResourceList<SelectOption>(results);
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/export/{id}", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@AuthenticatedContext
	public String exportResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException,
			ResourceExportException {

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ resourceService.getResourceCategory() + "-"
				+ resourceService.getResourceById(id).getName() + ".json\"");
		return resourceService.exportResoure(id);

	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/export", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@AuthenticatedContext
	public String exportAll(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException,
			ResourceNotFoundException, ResourceExportException {

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ resourceService.getResourceCategory() + ".json\"");
		return resourceService.exportAllResoures();

	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/import", method = { RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<TriggerResource> importAll(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile jsonFile,
			@RequestParam(required = false) boolean dropExisting)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		try {
			String json = IOUtils.toString(jsonFile.getInputStream());
			if (!HypersocketUtils.isValidJSON(json)) {
				throw new ResourceException(
						I18NServiceImpl.USER_INTERFACE_BUNDLE,
						"error.incorrectJSON");
			}
			Collection<TriggerResource> collects = resourceService
					.importResources(json, getCurrentRealm(), dropExisting);
			return new ResourceStatus<TriggerResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					I18NServiceImpl.USER_INTERFACE_BUNDLE,
					"resource.import.success", collects.size()));
		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false, e.getMessage());
		} catch (Exception e) {
			return new ResourceStatus<TriggerResource>(false, I18N.getResource(
					sessionUtils.getLocale(request),
					I18NServiceImpl.USER_INTERFACE_BUNDLE,
					"resource.import.failure", e.getMessage()));
		} 
	}

	@AuthenticationRequiredButDontTouchSession
	@RequestMapping(value = "triggers/image/{uuid}", method = RequestMethod.GET, produces = { "application/json" })
	@AuthenticatedContext
	public void downloadTemplateImage(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String uuid)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, IOException {

		resourceService.downloadTemplateImage(uuid, request, response);
	}

	@AuthenticationRequired
	@RequestMapping(value = "triggers/search", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> search(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException,
			NumberFormatException, IOException {

		String json = resourceService.searchTemplates(
				request.getParameter("sSearch"),
				Integer.parseInt(request.getParameter("iDisplayStart")),
				Integer.parseInt(request.getParameter("iDisplayLength")));
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(json, BootstrapTableResult.class);
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "triggers/script", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<TriggerResource> createFromScript(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String script) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			TriggerResource newResource = resourceService
					.importResources(script, getCurrentRealm(), false).iterator()
					.next();

			return new ResourceStatus<TriggerResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							TriggerResourceServiceImpl.RESOURCE_BUNDLE,
							"resource.created.info", newResource.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false,
					e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "triggers/eventsTable", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableEvents(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return TriggerResourceColumns.valueOf(col.toUpperCase());
					}

					@Override
					public List<?> getPage(String searchColumn, String searchPattern, int start,
							int length, ColumnSort[] sorting)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.getTriggerEvents(searchPattern, sessionUtils.getLocale(request));
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.getTriggerEventCount(searchPattern, sessionUtils.getLocale(request));
					}
				});
	}
	
	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "triggers/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus deleteResources(HttpServletRequest request,
												HttpServletResponse response,
												@RequestBody Long[] ids)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		try {
			
			if(ids == null) {
				ids = new Long[0];
			}
			
			List<TriggerResource> triggerResources = resourceService.getResourcesByIds(ids);

			if(triggerResources == null || triggerResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				resourceService.deleteResources(triggerResources);
				return new RequestStatus(true,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.success"));
			}
			
		} catch (Exception e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
}
