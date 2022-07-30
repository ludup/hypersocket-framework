package com.hypersocket.dashboard.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.dashboard.Link;
import com.hypersocket.dashboard.OverviewWidget;
import com.hypersocket.dashboard.OverviewWidgetService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class OverviewWidgetController extends ResourceController {

	@Autowired
	private OverviewWidgetService service;

	@AuthenticationRequired
	@RequestMapping(value = "overview/widgets/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<OverviewWidget> getWidgets(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<OverviewWidget>(service.getWidgets(resourceKey));
	}

	@AuthenticationRequired
	@RequestMapping(value = "overview/allWidgets/{resourceKey}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<OverviewWidget> getAllWidgets(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<OverviewWidget>(service.getAllWidgets(resourceKey));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "overview/articles", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<Link> getArticles(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

				return new ResourceList<Link>(service.getLinks());
			
		} catch (ResourceException e) {
			return new ResourceList<Link>(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "overview/videos", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<Link> getVideos(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {
			return new ResourceList<Link>(service.getVideos());
		} catch (ResourceException e) {
			return new ResourceList<Link>(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "overview/documentation", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<Link> getDocumentation(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {
			return new ResourceList<Link>(service.getDocumentation());
		} catch (ResourceException e) {
			return new ResourceList<Link>(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "overview/firstSteps", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<Link> getFirstSteps(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		try {
			return new ResourceList<Link>(service.getFirstSteps());
		} catch (ResourceException e) {
			return new ResourceList<Link>(false, e.getMessage());
		}
	}
}
