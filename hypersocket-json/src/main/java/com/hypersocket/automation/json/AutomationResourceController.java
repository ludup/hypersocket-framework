package com.hypersocket.automation.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.automation.AutomationResourceColumns;
import com.hypersocket.automation.AutomationResourceService;
import com.hypersocket.automation.AutomationResourceServiceImpl;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.ResourceUpdate;
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
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerResultType;
import com.hypersocket.triggers.TriggerType;
import com.hypersocket.triggers.json.AbstractTriggerController;
import com.hypersocket.triggers.json.TriggerResourceUpdate;
import com.hypersocket.utils.HypersocketUtils;

@Controller
public class AutomationResourceController extends AbstractTriggerController {

	static Logger log = LoggerFactory.getLogger(AutomationResourceController.class);
	
	@Autowired
	private AutomationResourceService resourceService;

	@Autowired
	private TriggerResourceService triggerService; 
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<AutomationResource> getResources(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<AutomationResource>(
				resourceService.getResources(sessionUtils
						.getCurrentRealm(request)));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableNetworkResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return AutomationResourceColumns.valueOf(col.toUpperCase());
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
	@RequestMapping(value = "automations/tasks", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<TaskDefinition> getActions(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		List<TaskDefinition> result = new ArrayList<>();
		for(TaskDefinition task : resourceService.getTasks()) {
			result.add(task);
		}
		return new ResourceList<>(result);
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/template/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resourceKey));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/run/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus runResource(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		try {
			AutomationResource resource = resourceService.getResourceById(id);
			resourceService.runNow(resource);
			return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request), 
					AutomationResourceServiceImpl.RESOURCE_BUNDLE,
					"info.startedAutomation", resource.getName()));
		} catch(Exception ex) { 
			log.error("Failed to run automation on demand", ex);
			return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request), 
					AutomationResourceServiceImpl.RESOURCE_BUNDLE,
					"error.failedToStartAutomation", ex.getMessage()));
		}
	}
	@AuthenticationRequired
	@RequestMapping(value = "automations/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		AutomationResource resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/automation/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public AutomationResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {
		return resourceService.getResourceById(id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/automation", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<AutomationResource> createOrUpdateResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody ResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			AutomationResource newResource;

			Realm realm = sessionUtils.getCurrentRealm(request);

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}
			
			if (resource.getId() != null) {
				newResource = resourceService.updateResource(
						resourceService.getResourceById(resource.getId()),
						resource.getName(), properties);
			} else {
				newResource = resourceService.createResource(
						resource.getName(),
						realm,
						null,
						properties);
			}
			return new ResourceStatus<AutomationResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							AutomationResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<AutomationResource>(false,
					e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/trigger", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<AutomationResource> createOrUpdateTriggerResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody TriggerResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			AutomationResource parentResource = null;
			TriggerResource parentTrigger = null;
			Realm realm = sessionUtils.getCurrentRealm(request);
			
			List<TriggerCondition> allConditions = new ArrayList<TriggerCondition>();
			List<TriggerCondition> anyConditions = new ArrayList<TriggerCondition>();
			
			processConditions(resource, allConditions, anyConditions);
			
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}
			
			if(resource.getType()==TriggerType.AUTOMATION) {
				parentResource = resourceService.getResourceById(resource.getParentId());	
			} else {
				parentTrigger = triggerService.getResourceById(resource.getParentId());
			}
			
			if(parentResource==null) {
				TriggerResource tmp = parentTrigger;
				while(tmp.getParentTrigger()!=null) {
					tmp = tmp.getParentTrigger();
				}	
				parentResource = resourceService.getResourceById(tmp.getAttachmentId());
			} 
			
			if (resource.getId() != null) {
				parentResource = resourceService.updateTrigger(
						triggerService.getResourceById(resource.getId()),
						resource.getName(), resource.getEvent(),
						TriggerResultType.valueOf(resource.getResult()),
						resource.getTask(), properties, allConditions,
						anyConditions, parentTrigger, parentResource);
			} else {
				parentResource = resourceService.createTrigger(
						resource.getName(), resource.getEvent(),
						TriggerResultType.valueOf(resource.getResult()), resource.getTask(), properties, realm,
						allConditions, anyConditions, parentTrigger, parentResource);
			}
			
			return new ResourceStatus<AutomationResource>(parentResource,
					I18N.getResource(sessionUtils.getLocale(request),
							AutomationResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<AutomationResource>(false, e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "automations/automation/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<AutomationResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			AutomationResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<AutomationResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								AutomationResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<AutomationResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					AutomationResourceServiceImpl.RESOURCE_BUNDLE,
					"resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<AutomationResource>(false, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "automations/trigger/{id}/{triggerId}", method = RequestMethod.DELETE, produces = { "application/json" })

	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<TriggerResource> deleteTrigger(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id, @PathVariable("triggerId") Long triggerId) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			TriggerResource resource = triggerService.getResourceById(triggerId);

			if (resource == null) {
				return new ResourceStatus<TriggerResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", triggerId));
			}
			
			AutomationResource automation = resourceService.getResourceById(id);
			if (automation == null) {
				return new ResourceStatus<TriggerResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", triggerId));
			}
			

			String preDeletedName = resource.getName();

			TriggerResource rootTrigger = null;

			if (resource.getParentTrigger() != null) {
				rootTrigger = resource;
				while (rootTrigger.getParentTrigger() != null) {
					rootTrigger = rootTrigger.getParentTrigger();
				}
			} else {
				automation.getChildTriggers().remove(resource);
				resourceService.updateResource(automation);
			}

			triggerService.deleteResource(resource);

			if (rootTrigger != null) {
				return new ResourceStatus<TriggerResource>(
						triggerService.getResourceById(rootTrigger.getId()),
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"trigger.deleted.info", preDeletedName));
			} else {
				return new ResourceStatus<TriggerResource>(true,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"trigger.deleted.info", preDeletedName));
			}

		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false, e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "automations/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
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
			
			List<AutomationResource> automationResources = resourceService.getResourcesByIds(ids);

			if(automationResources == null || automationResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				resourceService.deleteResources(automationResources);
				return new RequestStatus(true,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.success"));
			}
			
		} catch (Exception e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/export/{id}", method = RequestMethod.GET, produces = { "text/plain" })
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
	@RequestMapping(value = "automations/export", method = RequestMethod.GET, produces = { "text/plain" })
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
	@RequestMapping(value = "automations/import", method = { RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<AutomationResource> importAll(
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
			Collection<AutomationResource> collects = resourceService
					.importResources(json, getCurrentRealm(), dropExisting);
			return new ResourceStatus<AutomationResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					I18NServiceImpl.USER_INTERFACE_BUNDLE,
					"resource.import.success", collects.size()));
		} catch (ResourceException e) {
			return new ResourceStatus<AutomationResource>(false, e.getMessage());
		} catch (Exception e) {
			return new ResourceStatus<AutomationResource>(false, I18N.getResource(
					sessionUtils.getLocale(request),
					I18NServiceImpl.USER_INTERFACE_BUNDLE,
					"resource.import.failure", e.getMessage()));
		}
	}
}
