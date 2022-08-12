/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.Cacheable;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18NGroup;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionUtils;

@Controller
public class I18NController extends AuthenticatedController {

	@Autowired
	private I18NService i18nService;
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@RequestMapping(value="i18n", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@Cacheable
	public Map<String,String> getResources(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, AccessDeniedException {
		
		return getResourcesWithGroup(webRequest, request, response, I18NGroup.DEFAULT_GROUP.getTitle());
	}
	
	@RequestMapping(value="i18n/group/{group}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@AuthenticatedContext(anonymous = true)
	@Cacheable
	public Map<String,String> getResourcesWithGroup(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response, @PathVariable String group) throws IOException, AccessDeniedException {
		var rebuildI18N = (Boolean)request.getSession().getAttribute(SessionUtils.HYPERSOCKET_REBUILD_I18N);
		/* If the locale has just been changed, never return SC_NOT_MODIFIED, we
		 * need to send the map for the new locale */
		if(!Boolean.TRUE.equals(rebuildI18N) && webRequest.checkNotModified(i18nService.getLastUpdate())) {
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
		}
		else if(Boolean.TRUE.equals(rebuildI18N)) {
			/* Don't rebuild next time, client will have new map */
			request.getSession().removeAttribute(SessionUtils.HYPERSOCKET_REBUILD_I18N);
		}
		var results = i18nService.getResourceMap(sessionUtils.getLocale(request), I18NGroup.fromTitle(group));
		results.put("LANG", sessionUtils.getLocale(request).getLanguage());
		return serializeCache(results);
	}
	
	@RequestMapping(value="i18n/{locale}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	@Cacheable
	public Map<String,String> getResources(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response, @PathVariable String locale) throws IOException, AccessDeniedException {
		
		return getResourcesWithGroupAndLocale(webRequest, request, response, locale, I18NGroup.DEFAULT_GROUP.getTitle());
	}
	
	@RequestMapping(value="i18n/locale/{locale}/group/{group}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	@AuthenticatedContext(anonymous = true)
	public Map<String,String> getResourcesWithGroupAndLocale(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response,
			@PathVariable("locale") String locale, @PathVariable("group") String group) throws IOException, AccessDeniedException {
		
		if(webRequest.checkNotModified(i18nService.getLastUpdate())) {
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
		}
		var results = i18nService.getResourceMap(i18nService.getLocale(locale), I18NGroup.fromTitle(group));
		results.put("LANG", locale);
		return serializeCache(results);
	}
	
	@RequestMapping(value = "i18n/allLanguageTags", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@AuthenticatedContext(anonymous = true)
	public ResourceList<SelectOption> getAllLanguageTags(
			WebRequest webRequest, HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, IOException {

		var locales = new ArrayList<SelectOption>();
		for (var l : Locale.getAvailableLocales()) {
			var n = l.getDisplayLanguage();
			if(StringUtils.isNotBlank(l.getDisplayCountry()) && !Objects.equals(n, l.getDisplayCountry()))
				n += " (" + l.getDisplayCountry() + ")";
			if(StringUtils.isNotBlank(l.getVariant()))
				n += " [" + l.getDisplayCountry() + "]";
			locales.add(new SelectOption(l.toLanguageTag(), n));
		}
		Collections.sort(locales);
		return new ResourceList<>(locales);
	}
	
	@RequestMapping(value = "i18n/locales", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@AuthenticatedContext(anonymous = true)
	public ResourceList<SelectOption> getLocales(
			WebRequest webRequest, HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, IOException {

		if(webRequest.checkNotModified(i18nService.getLastUpdate())) {
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
		}
		return new ResourceList<>(i18nService.getSupportedLocales().stream()
				.map(l -> new SelectOption(l.getLanguage(), l.getDisplayLanguage())).collect(Collectors.toList()));
	}
	
	private Map<String,String> serializeCache(Cache<String,String> cache) throws JsonGenerationException, JsonMappingException, IOException {
		Map<String,String> m = new HashMap<String,String>();
		for(Iterator<Entry<String,String>> it = cache.iterator(); it.hasNext();) {
			Entry<String,String> e = it.next();
			m.put(e.getKey(), e.getValue());
		}
		return m;

	}
	
}
