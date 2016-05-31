package com.hypersocket.automation.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.automation.AutomationResourceColumns;
import com.hypersocket.automation.AutomationResourceService;
import com.hypersocket.automation.AutomationResourceServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.ResourceUpdate;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerResultType;
import com.hypersocket.triggers.TriggerType;
import com.hypersocket.triggers.json.AbstractTriggerController;
import com.hypersocket.triggers.json.TriggerResourceUpdate;

@Controller
public class AutomationResourceController extends AbstractTriggerController {


	@Autowired
	AutomationResourceService resourceService;

	@Autowired
	TriggerResourceService triggerService; 
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<AutomationResource> getResources(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceList<AutomationResource>(
					resourceService.getResources(sessionUtils
							.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableNetworkResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
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
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/tasks", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getActions(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			List<SelectOption> result = new ArrayList<SelectOption>();
			for(String task : resourceService.getTasks()) {
				result.add(new SelectOption(task, task));
			}
			return new ResourceList<SelectOption>(result);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/template/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resourceKey));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "automations/run/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus runResource(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			AutomationResource resource = resourceService.getResourceById(id);
			resourceService.runNow(resource);
			return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request), 
					AutomationResourceServiceImpl.RESOURCE_BUNDLE,
					"info.startedAutomation", resource.getName()));
		} catch(Exception ex) { 
			return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request), 
					AutomationResourceServiceImpl.RESOURCE_BUNDLE,
					"error.failedToStartAutomation", ex.getMessage()));
		} finally {
			clearAuthenticatedContext();
		}
	}
	@AuthenticationRequired
	@RequestMapping(value = "automations/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			AutomationResource resource = resourceService.getResourceById(id);
			return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/automation/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AutomationResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return resourceService.getResourceById(id);
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/automation", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<AutomationResource> createOrUpdateResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody ResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
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
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/trigger", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<AutomationResource> createOrUpdateTriggerResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody TriggerResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
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
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "automations/automation/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<AutomationResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
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
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "automations/trigger/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<TriggerResource> deleteTrigger(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			TriggerResource resource = triggerService.getResourceById(id);

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

			AutomationResource automation = resourceService.getResourceById(resource.getAttachmentId());
			
			automation.getChildTriggers().remove(resource);
			resourceService.updateResource(automation);
			triggerService.deleteResource(resource);

			return new ResourceStatus<TriggerResource>(true,
						I18N.getResource(sessionUtils.getLocale(request),
								TriggerResourceServiceImpl.RESOURCE_BUNDLE,
								"resource.deleted.info", preDeletedName));
		
		} catch (ResourceException e) {
			return new ResourceStatus<TriggerResource>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
}
