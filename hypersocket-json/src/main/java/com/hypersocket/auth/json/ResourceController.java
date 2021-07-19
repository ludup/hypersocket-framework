/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.ResourceStatusConfirmation;
import com.hypersocket.json.ResourceStatusRedirect;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceConfirmationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceRedirectException;
import com.hypersocket.tables.json.BootstrapTableController;

@Controller
public class ResourceController extends BootstrapTableController<Resource> {

	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@ExceptionHandler(ResourceConfirmationException.class)
	@Order(0)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public <T>  ResourceStatusConfirmation<T> resourceConfirmation(HttpServletRequest request,
			HttpServletResponse response, ResourceStatusConfirmation<?> e) {
		log.debug("Resource confirmation.", e);
		return new ResourceStatusConfirmation<T>(e.getMessage(), e.getOptions(), e.getArgs());
	}

	@Order(1)
	@ResponseBody
	@ExceptionHandler(ResourceRedirectException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public <T> ResourceStatusRedirect<T> resourceRedirect(HttpServletRequest request,
			HttpServletResponse response, ResourceRedirectException e) {
		log.debug("Resource redirect.", e);
		return new ResourceStatusRedirect<T>(e.getMessage());
	}

	@Order(9999)
	@ResponseBody
	@ExceptionHandler(ResourceException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public <T> ResourceStatus<T> resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceException e) {
		log.error("Resource error", e);
		return new ResourceStatus<T>(false, e.getMessage());
	}

}
