package com.hypersocket.delegation.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.delegation.UserDelegationResourceColumns;
import com.hypersocket.delegation.UserDelegationResourceService;
import com.hypersocket.delegation.UserDelegationResourceServiceImpl;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.RoleUtils;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AssignableResourceUpdate;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class UserDelegationResourceController extends ResourceController {

	@Autowired
	private UserDelegationResourceService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<UserDelegationResource> getResources(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<UserDelegationResource>(
				resourceService.getResources(sessionUtils
						.getCurrentRealm(request)));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/myUserDelegations", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<UserDelegationResource> getResourcesByCurrentPrincipal(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return new ResourceList<UserDelegationResource>(
				resourceService.getResources(sessionUtils
						.getPrincipal(request)));
	}

	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return UserDelegationResourceColumns.valueOf(col.toUpperCase());
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
	@RequestMapping(value = "userDelegations/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		UserDelegationResource resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/userDelegation/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public UserDelegationResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		return resourceService.getResourceById(id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/userDelegation", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<UserDelegationResource> createOrUpdateResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody AssignableResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			UserDelegationResource newResource;

			Realm realm = sessionUtils.getCurrentRealm(request);

			Set<Role> roles = RoleUtils.processPermissions(resource.getRoles());
			
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			if (resource.getId() != null) {
				newResource = resourceService.updateResource(
						resourceService.getResourceById(resource.getId()),
						resource.getName(), roles, properties);
			} else {
				newResource = resourceService.createResource(
						resource.getName(), roles,
						realm, properties);
			}
			return new ResourceStatus<UserDelegationResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							UserDelegationResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<UserDelegationResource>(false,
					e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/userDelegation/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<UserDelegationResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			UserDelegationResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<UserDelegationResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								UserDelegationResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<UserDelegationResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					UserDelegationResourceServiceImpl.RESOURCE_BUNDLE,
					"resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<UserDelegationResource>(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "userDelegations/personal", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> personalResources(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return UserDelegationResourceColumns.valueOf(col.toUpperCase());
					}

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start, int length,
							ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
						return resourceService.searchPersonalResources(sessionUtils.getPrincipal(request),
								searchColumn, searchPattern, start, length, sorting);
					}
					
					@Override
					public Long getTotalCount(String searchColumn, String searchPattern) throws UnauthorizedException, AccessDeniedException {
						return resourceService.getPersonalResourceCount(
								sessionUtils.getPrincipal(request),
								searchColumn, searchPattern);
					}
				});
	}
}
