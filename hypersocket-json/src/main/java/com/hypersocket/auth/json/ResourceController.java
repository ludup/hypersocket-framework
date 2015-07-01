/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.json.BootstrapTableController;

@Controller
public class ResourceController extends BootstrapTableController {

	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@ExceptionHandler(ResourceChangeException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceChangeException e) {
		log.error("Resource change error", e);
	}

	@ExceptionHandler(ResourceCreationException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceCreationException e) {
		log.error("Resource creation error", e);
	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceNotFoundException e) {
		log.error("Resource not found error", e);
	}
	
}
