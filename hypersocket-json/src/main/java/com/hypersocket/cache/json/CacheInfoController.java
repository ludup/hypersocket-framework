package com.hypersocket.cache.json;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.SchedulerException;
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
import com.hypersocket.cache.CacheInfo;
import com.hypersocket.cache.CacheInfoColumns;
import com.hypersocket.cache.CacheInfoService;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class CacheInfoController extends ResourceController {

	@Autowired
	private CacheInfoService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<CacheInfo> getResources(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, IOException {
		return new ResourceList<>(resourceService.getResources(sessionUtils.getCurrentRealm(request)));
	}

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableResources(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

			@Override
			public Column getColumn(String col) {
				return CacheInfoColumns.valueOf(col.toUpperCase());
			}

			@Override
			public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
					ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
				try {
					return resourceService.searchResources(sessionUtils.getCurrentRealm(request), searchColumn,
							searchPattern, start, length, sorting);
				} catch (IOException e) {
					throw new IllegalStateException("Failed to get page.", e);
				}
			}

			@Override
			public Long getTotalCount(String searchColumn, String searchPattern)
					throws UnauthorizedException, AccessDeniedException {
				return resourceService.getResourceCount(sessionUtils.getCurrentRealm(request), searchColumn,
						searchPattern);
			}
		});
	}

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(HttpServletRequest request, @PathVariable String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, ResourceNotFoundException,
			SchedulerException, NotScheduledException, IOException {
		var resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/cache/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public CacheInfo getResource(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id)
			throws AccessDeniedException, UnauthorizedException, ResourceNotFoundException, SessionTimeoutException,
			SchedulerException, NotScheduledException, IOException {
		return resourceService.getResourceById(id);
	}

	@RequestMapping(value = "cacheInfo/cache/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<CacheInfo> deleteResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, IOException {
		try {
			CacheInfo resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<>(false, I18N.getResource(sessionUtils.getLocale(request),
						CacheInfoService.RESOURCE_BUNDLE, "error.invalidResourceId", id));
			}

			String preDeletedName = resource.getId();
			resourceService.deleteResource(resource);

			return new ResourceStatus<>(true, I18N.getResource(sessionUtils.getLocale(request),
					CacheInfoService.RESOURCE_BUNDLE, "resource.deleted.info", preDeletedName));

		} catch (IOException e) {
			return new ResourceStatus<CacheInfo>(false, e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "cacheInfo/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus deleteResources(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		try {

			if (ids == null) {
				ids = new String[0];
			}

			var messageResources = resourceService.getResourcesByIds(ids);

			if (messageResources == null || messageResources.isEmpty()) {
				return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request),
						CacheInfoService.RESOURCE_BUNDLE, "bulk.delete.empty"));
			} else {
				resourceService.deleteResources(messageResources);
				return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request),
						CacheInfoService.RESOURCE_BUNDLE, "bulk.delete.success"));
			}

		} catch (Exception e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
}
