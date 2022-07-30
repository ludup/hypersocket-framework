package com.hypersocket.scheduler.json;

import java.io.IOException;
import java.util.ArrayList;
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
import com.hypersocket.utils.HypersocketUtils;

@Controller
public class AbstractSchedulerResourceController extends ResourceController {

	public ResourceList<SchedulerResource> getResources(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, SchedulerException {

		return new ResourceList<SchedulerResource>(
				resourceService.getResources(sessionUtils.getCurrentRealm(request)));
	}

	public BootstrapTableResult<?> tableResources(SchedulerService resourceService, final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

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
	}

	public ResourceList<PropertyCategory> getResourceTemplate(SchedulerService resourceService,
			HttpServletRequest request) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}

	public ResourceList<PropertyCategory> getActionTemplate(SchedulerService resourceService,
			HttpServletRequest request, String id) throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException, SchedulerException, NotScheduledException, IOException {
		SchedulerResource resource = resourceService.getResourceById(HypersocketUtils.base64DecodeToString(id));
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	public SchedulerResource getResource(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException, IOException {

		return resourceService.getResourceById(HypersocketUtils.base64DecodeToString(id));
	}
	
	public void fireJob(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException, IOException {

		resourceService.fireJob(resourceService.getResourceById(HypersocketUtils.base64DecodeToString(id)).getName());
	}
	
	public void interruptJob(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException, IOException {

		resourceService.interrupt(resourceService.getResourceById(HypersocketUtils.base64DecodeToString(id)).getName());

	}

	public ResourceStatus<SchedulerResource> deleteResource(SchedulerService resourceService,
			HttpServletRequest request, HttpServletResponse response, String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, IOException {

		try {

			SchedulerResource resource = resourceService.getResourceById(id = HypersocketUtils.base64DecodeToString(id));

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
		} 
	}

	public RequestStatus deleteResources(SchedulerService resourceService, HttpServletRequest request,
			HttpServletResponse response, String[] ids)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		try {

			if (ids == null) {
				ids = new String[0];
			}
			
			List<String> tmp = new ArrayList<>();
			for(String id : ids) {
				tmp.add(HypersocketUtils.base64DecodeToString(id));
			}

			List<SchedulerResource> messageResources = resourceService.getResourcesByIds(tmp.toArray(new String[0]));

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
		} 
	}
}
