package com.hypersocket.interfaceState.json;

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
import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.interfaceState.UserInterfaceStateService;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class UserInterfaceStateController extends ResourceController {

	@Autowired
	UserInterfaceStateService service;

	/*
	 * @AuthenticationRequired
	 * 
	 * @RequestMapping(value = "interfaceState/state/{specific}/{resources}",
	 * method = RequestMethod.GET, produces = { "application/json" })
	 * 
	 * @ResponseBody
	 * 
	 * @ResponseStatus(value = HttpStatus.OK) public
	 * ResourceList<UserInterfaceState> getStates( HttpServletRequest request,
	 * 
	 * @PathVariable("specific") boolean specific,
	 * 
	 * @PathVariable("resources") Long[] resources) throws
	 * AccessDeniedException, UnauthorizedException, SessionTimeoutException {
	 * setupAuthenticatedContext(sessionUtils.getSession(request),
	 * sessionUtils.getLocale(request)); try { return new
	 * ResourceList<UserInterfaceState>(service.getStates( resources)); }
	 * finally { clearAuthenticatedContext(); } }
	 */
	/*
	 * @AuthenticationRequired
	 * 
	 * @RequestMapping(value = "interfaceState/state", method =
	 * RequestMethod.POST, produces = { "application/json" })
	 * 
	 * @ResponseBody
	 * 
	 * @ResponseStatus(value = HttpStatus.OK) public
	 * ResourceStatus<UserInterfaceState> saveState( HttpServletRequest request,
	 * HttpServletResponse response,
	 * 
	 * @RequestBody UserInterfaceStateUpdate userInterfaceStateUpdate) throws
	 * AccessDeniedException, UnauthorizedException, SessionTimeoutException {
	 * 
	 * setupAuthenticatedContext(sessionUtils.getSession(request),
	 * sessionUtils.getLocale(request)); try {
	 * 
	 * UserInterfaceState newState = service
	 * .getStateByResourceId(userInterfaceStateUpdate .getResourceId());
	 * 
	 * if (newState != null) { newState = service.updateState(newState,
	 * userInterfaceStateUpdate.getName(),
	 * userInterfaceStateUpdate.getProperties()); } else { newState =
	 * service.createState( userInterfaceStateUpdate.getResourceId(),
	 * userInterfaceStateUpdate.getName(),
	 * userInterfaceStateUpdate.getProperties()); } return new
	 * ResourceStatus<UserInterfaceState>(newState,
	 * I18N.getResource(sessionUtils.getLocale(request),
	 * UserInterfaceStateServiceImpl.RESOURCE_BUNDLE, newState.getId() != null ?
	 * "attribute.updated.info" : "attribute.created.info", newState
	 * .getName())); // } catch (ResourceException e) { // return new
	 * ResourceStatus<UserInterfaceState>(false, // e.getMessage());
	 * 
	 * } finally { clearAuthenticatedContext(); } }
	 */
	@AuthenticationRequired
	@RequestMapping(value = "interfaceState/tableState/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserInterfaceState> getTableStates(
			HttpServletRequest request, @PathVariable("name") String name)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			UserInterfaceState userInterfaceState = service.getState(name);
			return new ResourceStatus<UserInterfaceState>(userInterfaceState,
					userInterfaceState.getName());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "interfaceState/tableState", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserInterfaceState> saveTableState(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserInterfaceStateUpdate userInterfaceState)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			UserInterfaceState newState = service.getState(userInterfaceState
					.getName() + "_" + getCurrentPrincipal().getId());
			if (newState != null) {
				newState = service.updateState(newState,
						userInterfaceState.getPreferences());
			} else {
				newState = service.createState(getCurrentPrincipal().getId(),
						userInterfaceState.getPreferences(),
						userInterfaceState.getName());
			}

			return new ResourceStatus<UserInterfaceState>(newState,
					newState.getName());
		} catch (Exception e) {
			return new ResourceStatus<UserInterfaceState>(false, e.getMessage());

		} finally {
			clearAuthenticatedContext();
		}
	}
}
