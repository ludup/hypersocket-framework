/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

public class ResourceChangeException extends ResourceException {

	private static final long serialVersionUID = -3617137415580739443L;
	
	public ResourceChangeException(ResourceException e) {
		super(e.getBundle(), e.getResourceKey(), e.getArgs());
	}
	
	public ResourceChangeException(String bundle, String resourceKey, Object... args) {
		super(bundle, resourceKey, args);
	}
}
