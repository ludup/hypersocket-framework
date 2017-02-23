/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions.json;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
public class PermissionsController extends ResourceController {

	@Autowired
	PermissionService permissionService;

	
	@AuthenticationRequired
	@RequestMapping(value = "permissions/permission/{resourceKey}/", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Permission> getPermission(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String resourceKey)
			throws AccessDeniedException, UnauthorizedException {

		permissionService.setCurrentSession(
				sessionUtils.getActiveSession(request),
				sessionUtils.getLocale(request));
		try {
		
			return new ResourceStatus<Permission>(
					permissionService.getPermission(resourceKey));
		
		} finally {
			permissionService.clearPrincipalContext();
		}
		
	}

	@AuthenticationRequired
	@RequestMapping(value = "permissions/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public PermissionList listPermissions(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException {
		
		permissionService.setCurrentSession(
				sessionUtils.getActiveSession(request),
				sessionUtils.getLocale(request));
		try {
		
			return new PermissionList(permissionService.allPermissions());
		
		} finally {
			permissionService.clearPrincipalContext();
		}
	}
	
	
	@AuthenticationRequired
	@RequestMapping(value = "permissions/verify/{permissions}", method = RequestMethod.GET,
			produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus verifyPermissions(HttpServletRequest request,
											  HttpServletResponse response,
											  @PathVariable String[] permissions) throws AccessDeniedException,
			UnauthorizedException, IOException {

		permissionService.setCurrentSession(
				sessionUtils.getActiveSession(request),
				sessionUtils.getLocale(request));
		try {
			String mode = request.getParameter("mode");
			if(StringUtils.isBlank(mode)) {
				mode = "any";
			}
			List<Permission> perms = permissionService.getPermissions(permissions);
			if(perms == null) {
				throw new IOException("Unexpected permission resource key" + Arrays.toString(permissions));
			}

			if("any".equals(mode)) {
				return new RequestStatus(permissionService.hasAnyPermission(getCurrentPrincipal(),
						perms.toArray(new Permission[0])));
			}else if ("all".equals(mode)){
				return new RequestStatus(permissionService.hasAllPermissions(getCurrentPrincipal(),
						perms.toArray(new Permission[0])));
			} else{
				return new RequestStatus(false);
			}

		} finally {
			permissionService.clearPrincipalContext();
		}
	}
}
