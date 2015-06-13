package com.hypersocket.dashboard.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.dashboard.OverviewWidget;
import com.hypersocket.dashboard.OverviewWidgetService;
import com.hypersocket.dashboard.UsefulLink;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class OverviewWidgetController extends ResourceController {

	@Autowired
	OverviewWidgetService service;

	@AuthenticationRequired
	@RequestMapping(value = "overview/widgets", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<OverviewWidget> getWidgets(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceList<OverviewWidget>(service.getWidgets());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "overview/articles", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<UsefulLink> getArticles(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

				return new ResourceList<UsefulLink>(service.getLinks());
			
		} catch (ResourceException e) {
			return new ResourceList<UsefulLink>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
}
