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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tables.json.BootstrapTableController;

@Controller
public class ResourceController extends BootstrapTableController<Resource> {

	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@ExceptionHandler(ResourceException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceException e) {
		log.error("Resource error", e);
	}

	
}
