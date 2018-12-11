package com.hypersocket.scheduler.json;

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
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;

@Controller
public class ClusteredSchedulerResourceController extends AbstractSchedulerResourceController {

	@Autowired
	private ClusteredSchedulerService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SchedulerResource> getResources(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, SchedulerException {
		return super.getResources(resourceService, request, response);
	}

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableResources(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.tableResources(resourceService, request, response);
	}

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getResourceTemplate(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.getResourceTemplate(resourceService, request);
	}

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getActionTemplate(HttpServletRequest request, @PathVariable String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, ResourceNotFoundException,
			SchedulerException, NotScheduledException {
		return super.getActionTemplate(resourceService, request, id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/job/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public SchedulerResource getResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException, SchedulerException, NotScheduledException {
		return super.getResource(resourceService, request, response, id);
	}

	@RequestMapping(value = "clusteredScheduler/job/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<SchedulerResource> deleteResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.deleteResource(resourceService, request, response, id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus deleteResources(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.deleteResources(resourceService, request, response, ids);
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/fire/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus fireJob(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) {
		try {
			super.fireJob(resourceService, request, response, id);
			return new RequestStatus(true);
		} catch (ResourceNotFoundException | AccessDeniedException | UnauthorizedException | SessionTimeoutException
				| SchedulerException | NotScheduledException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "clusteredScheduler/interrupt/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus interruptJob(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) {
		try {
			super.interruptJob(resourceService, request, response, id);
			return new RequestStatus(true);
		} catch (ResourceNotFoundException | AccessDeniedException | UnauthorizedException | SessionTimeoutException
				| SchedulerException | NotScheduledException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
}
