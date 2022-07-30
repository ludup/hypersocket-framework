package com.hypersocket.services.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.service.ManageableService;
import com.hypersocket.service.ServiceManagementService;
import com.hypersocket.service.ServiceStatus;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class ServiceManagementController extends AuthenticatedController {

	@Autowired
	private ServiceManagementService managementService; 
	
	@AuthenticationRequired
	@RequestMapping(value = "services/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<ServiceStatus> listServices(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		List<ServiceStatus> result = new ArrayList<ServiceStatus>();
		for(ManageableService m : managementService.getServices(getCurrentRealm())) {
			for(ServiceStatus s : m.getStatus()) {
				result.add(new StatusView(s));
			}
		}
		return new ResourceList<ServiceStatus>(result);

	}
	
}
