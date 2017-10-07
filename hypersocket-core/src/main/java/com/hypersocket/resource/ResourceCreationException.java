/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;


public class ResourceCreationException extends ResourceException {

	private static final long serialVersionUID = 7411495296618455618L;
	
	public ResourceCreationException(ResourceException e) {
		super(e.getBundle(), e.getResourceKey(), e.getArgs());
	}
	
	public ResourceCreationException(String bundle, String resourceKey, Object... args) {
		super(bundle, resourceKey, args);
	}
	
	public ResourceCreationException(Throwable cause, String bundle, String resourceKey, Object... args) {
		super(cause, bundle, resourceKey, args);
	}
}
