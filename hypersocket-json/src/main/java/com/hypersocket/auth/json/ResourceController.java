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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.json.DataTablesController;

@Controller
public class ResourceController extends DataTablesController {

	@ExceptionHandler(ResourceChangeException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceChangeException redirect) {

	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public void resourceChangeError(HttpServletRequest request,
			HttpServletResponse response, ResourceNotFoundException redirect) {
	}
	
}
