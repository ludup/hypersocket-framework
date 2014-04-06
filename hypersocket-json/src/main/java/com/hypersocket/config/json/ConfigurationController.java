/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config.json;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class ConfigurationController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(ConfigurationController.class);

	@Autowired
	ConfigurationService configurationService;

	final static String RESOURCE_KEY = "ConfigurationController";

	@AuthenticationRequired
	@RequestMapping(value = "configuration", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getCategories(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return getCategories(request);
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "configuration", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus updateItems(HttpServletRequest request,
			HttpServletResponse response, @RequestBody PropertyItem[] items)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			
			Map<String,String> values=  new HashMap<String,String>();
			for(PropertyItem item : items) {
				values.put(item.getId(), item.getValue());
			}
			
			configurationService.setValues(values);
			
			return new RequestStatus(true, I18N.getResource(
					sessionUtils.getLocale(request), RESOURCE_KEY,
					"message.saved"));
		} catch (ResourceChangeException e) {
			return new RequestStatus(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	private ResourceList<PropertyCategory> getCategories(HttpServletRequest request)
			throws UnauthorizedException, AccessDeniedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceList<PropertyCategory>(
					configurationService.getPropertyCategories());
		} finally {
			clearAuthenticatedContext();
		}

	}
}
