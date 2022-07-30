package com.hypersocket.html.json;

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
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.html.HtmlTemplateResourceColumns;
import com.hypersocket.html.HtmlTemplateResourceService;
import com.hypersocket.html.HtmlTemplateResourceServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.ResourceUpdate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class HtmlTemplateResourceController extends ResourceController {

	@Autowired
	private HtmlTemplateResourceService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<HtmlTemplateResource> getResources(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<HtmlTemplateResource>(
				resourceService.getResources(sessionUtils
						.getCurrentRealm(request)));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/table", method = RequestMethod.GET, produces = { "application/json" })
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
						return HtmlTemplateResourceColumns.valueOf(col.toUpperCase());
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
	@RequestMapping(value = "htmlTemplates/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		HtmlTemplateResource resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/htmlTemplate/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public HtmlTemplateResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		return resourceService.getResourceById(id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/htmlTemplate", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<HtmlTemplateResource> createOrUpdateResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody ResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			HtmlTemplateResource newResource;

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
			return new ResourceStatus<HtmlTemplateResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							HtmlTemplateResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<HtmlTemplateResource>(false,
					e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "htmlTemplates/htmlTemplate/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<HtmlTemplateResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			HtmlTemplateResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<HtmlTemplateResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								HtmlTemplateResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<HtmlTemplateResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					HtmlTemplateResourceServiceImpl.RESOURCE_BUNDLE,
					"resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<HtmlTemplateResource>(false, e.getMessage());
		}
	}
}
