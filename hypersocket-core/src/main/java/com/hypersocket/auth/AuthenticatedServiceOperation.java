/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import com.hypersocket.permissions.AccessDeniedException;

public abstract class AuthenticatedServiceOperation<T extends AuthenticatedService, R> {
	
	T service;
	
	public AuthenticatedServiceOperation(T service) {
		this.service = service;
	}
	
	public T getService() {
		return service;
	}
	
	public abstract R execute(T service) throws AccessDeniedException;
}
