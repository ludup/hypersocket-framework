package com.hypersocket.scheduler.json;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;

import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.AbstractSchedulerServiceImpl;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.scheduler.SchedulerResourceColumns;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class AbstractSchedulerResourceController extends ResourceController {

	public ResourceList<SchedulerResource> getResources(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, SchedulerException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			return new ResourceList<SchedulerResource>(
					resourceService.getResources(sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	public BootstrapTableResult<?> tableResources(SchedulerService resourceService, final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

				@Override
				public Column getColumn(String col) {
					return SchedulerResourceColumns.valueOf(col.toUpperCase());
				}

				@Override
				public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
						ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
					return resourceService.searchResources(sessionUtils.getCurrentRealm(request), searchColumn,
							searchPattern, start, length, sorting);
				}

				@Override
				public Long getTotalCount(String searchColumn, String searchPattern)
						throws UnauthorizedException, AccessDeniedException {
					return resourceService.getResourceCount(sessionUtils.getCurrentRealm(request), searchColumn,
							searchPattern);
				}
			});
		} finally {
			clearAuthenticatedContext();
		}
	}

	public ResourceList<PropertyCategory> getResourceTemplate(SchedulerService resourceService,
			HttpServletRequest request) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
		} finally {
			clearAuthenticatedContext();
		}
	}

	public ResourceList<PropertyCategory> getActionTemplate(SchedulerService resourceService,
			HttpServletRequest request, String id) throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException, SchedulerException, NotScheduledException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			SchedulerResource resource = resourceService.getResourceById(id);
			return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
		} finally {
			clearAuthenticatedContext();
		}
	}

	public SchedulerResource getResource(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			return resourceService.getResourceById(id);
		} finally {
			clearAuthenticatedContext();
		}

	}
	
	public void fireJob(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			resourceService.fireJob(resourceService.getResourceById(id).getId());
		} finally {
			clearAuthenticatedContext();
		}

	}
	
	public void interruptJob(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			resourceService.interrupt(resourceService.getResourceById(id).getId());
		} finally {
			clearAuthenticatedContext();
		}

	}

	public ResourceStatus<SchedulerResource> deleteResource(SchedulerService resourceService,
			HttpServletRequest request, HttpServletResponse response, String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {

			SchedulerResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<SchedulerResource>(false, I18N.getResource(sessionUtils.getLocale(request),
						AbstractSchedulerServiceImpl.RESOURCE_BUNDLE, "error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<SchedulerResource>(true, I18N.getResource(sessionUtils.getLocale(request),
					AbstractSchedulerServiceImpl.RESOURCE_BUNDLE, "resource.deleted.info", preDeletedName));

		} catch (SchedulerException | NotScheduledException e) {
			return new ResourceStatus<SchedulerResource>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	public RequestStatus deleteResources(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String[] ids)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {

			if (ids == null) {
				ids = new String[0];
			}

			List<SchedulerResource> messageResources = resourceService.getResourcesByIds(ids);

			if (messageResources == null || messageResources.isEmpty()) {
				return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request),
						I18NServiceImpl.USER_INTERFACE_BUNDLE, "bulk.delete.empty"));
			} else {
				resourceService.deleteResources(messageResources);
				return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request),
						I18NServiceImpl.USER_INTERFACE_BUNDLE, "bulk.delete.success"));
			}

		} catch (Exception e) {
			return new RequestStatus(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
}
