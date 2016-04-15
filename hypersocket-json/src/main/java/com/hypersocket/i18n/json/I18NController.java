/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionUtils;

@Controller
public class I18NController extends AuthenticatedController {

	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	SessionUtils sessionUtils;
	
	@RequestMapping(value="i18n", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Map<String,String> getResources(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String,String> results = i18nService.getResourceMap(sessionUtils.getLocale(request));
		results.put("LANG", sessionUtils.getLocale(request).getLanguage());
		return results;
	}
	
	@RequestMapping(value="i18n/{locale}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Map<String,String> getResources(HttpServletRequest request, HttpServletResponse response, @PathVariable String locale) throws IOException {
		Map<String,String> results = i18nService.getResourceMap(i18nService.getLocale(locale));
		results.put("LANG", locale);
		return results;
	}	
	
	@RequestMapping(value = "i18n/locales", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getLocales(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException {

		List<SelectOption> locales = new ArrayList<SelectOption>();

		for (Locale l : i18nService.getSupportedLocales()) {
			locales.add(new SelectOption(l.getLanguage(), l.getDisplayLanguage()));
		}
		return new ResourceList<SelectOption>(locales);
	}
	
}
