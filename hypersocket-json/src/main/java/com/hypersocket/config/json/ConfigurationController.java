/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.ConfigurationServiceImpl;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class ConfigurationController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(ConfigurationController.class);

	final static String RESOURCE_KEY = "ConfigurationController";

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@AuthenticationRequired
	@RequestMapping(value = "configuration/timeZone/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<NameValuePair> getResources(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		Set<NameValuePair> tl = new HashSet<>();
		for (String tzid : TimeZone.getAvailableIDs()) {
			add(tl, TimeZone.getTimeZone(tzid));
		}
		add(tl, TimeZone.getDefault());
		return new ResourceList<NameValuePair>(tl);
	}

	protected void add(Set<NameValuePair> tl, TimeZone tz) {
		tl.add(new NameValuePair(String.format("%-30s (UTC %s)", tz.getID(),
				tz.getRawOffset() == 0 ? ""
						: (tz.getRawOffset() > 0 ? "+" : "") + ((tz.getRawOffset() / 1000 / 60 / 60) + ":00")
				), tz.getID()));
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getCategories(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return getCategories(request);
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/system", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getSystemCategories(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return getSystemCategories(request, "system");
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/system/{group}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getSystemCategoriesByGroup(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String group)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return getSystemCategories(request, group);
	}

	@RequestMapping(value = "configuration/values/{resourceKeys}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(anonymous = true)
	public ResourceStatus<Map<String, String>> getPropertyValues(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String resourceKeys)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		Realm realm = null;
		var results = new HashMap<String, String>();
		var authState = AuthenticationState.getCurrentState(request);
		if (authState != null && authState.getRealm() != null) {
			realm = authState.getRealm();
		}
		 
		if (realm == null) {
			realm = sessionUtils.getCurrentRealm(request);
		}
		
		for (var resourceKey : resourceKeys.split(",")) {
			results.put(resourceKey,
				configurationService.getValue(realm, resourceKey));
		}

		return new ResourceStatus<Map<String, String>>(results);
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/realm/{group}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getCategories(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String group) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return getCategories(request, group);
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/system", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus updateSystemItems(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PropertyItem[] items)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return updateSystemItems(request, response, items, "system");

	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/system/{group}", method = RequestMethod.POST, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus updateSystemItems(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PropertyItem[] items, @PathVariable String group)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {

			Map<String, String> values = new HashMap<String, String>();
			for (PropertyItem item : items) {
				values.put(item.getId(), item.getValue());
			}

			systemConfigurationService.setValues(values);

			return new RequestStatus(true,
					I18N.getResource(sessionUtils.getLocale(request), RESOURCE_KEY, "message.saved"));
		} catch (ResourceChangeException e) {
			return new RequestStatus(false, e.getMessage());
		} catch (Throwable t) {
			return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request),
					ConfigurationServiceImpl.RESOURCE_BUNDLE, "error.genericError", t.getMessage()));
		} 
	}

	@AuthenticationRequired
	@RequestMapping(value = "configuration/realm/{group}", method = RequestMethod.POST, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus updateRealmItems(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PropertyItem[] items, @PathVariable String group)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {

			Map<String, String> values = new HashMap<String, String>();
			for (PropertyItem item : items) {
				values.put(item.getId(), item.getValue());
			}

			configurationService.setValues(values);

			return new RequestStatus(true,
					I18N.getResource(sessionUtils.getLocale(request), RESOURCE_KEY, "message.saved"));
		} catch (ResourceChangeException e) {
			return new RequestStatus(false, e.getMessage());
		} catch (Throwable t) {
			return new RequestStatus(false, I18N.getResource(sessionUtils.getLocale(request),
					ConfigurationServiceImpl.RESOURCE_BUNDLE, "error.genericError", t.getMessage()));
		}
	}

	private ResourceList<PropertyCategory> getCategories(HttpServletRequest request)
			throws UnauthorizedException, AccessDeniedException, SessionTimeoutException {

		return new ResourceList<PropertyCategory>(configurationService.getPropertyCategories());
	}

	private ResourceList<PropertyCategory> getCategories(HttpServletRequest request, String group)
			throws UnauthorizedException, AccessDeniedException, SessionTimeoutException {

		return new ResourceList<PropertyCategory>(configurationService.getPropertyCategories(group));
	}

	private ResourceList<PropertyCategory> getSystemCategories(HttpServletRequest request, String group)
			throws UnauthorizedException, AccessDeniedException, SessionTimeoutException {

		ResourceList<PropertyCategory> result = new ResourceList<PropertyCategory>(
				systemConfigurationService.getPropertyCategories(group));
		return result;

	}
}
