/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

public class ResourceBadRequestException extends ResourceException {

	private static final long serialVersionUID = -5733235151026195782L;

	private final String code;

	public ResourceBadRequestException(String bundle, String resourceKey,String code,
                                       Object... args) {
		this(null, bundle, resourceKey, code, args);
	}
	
	public ResourceBadRequestException(Throwable cause, String bundle, String resourceKey,String code, 
            Object... args) {
		super(cause, bundle, resourceKey, args);
		this.code = code;
	}

	public String getCode(){
		return this.code;
	}
}
