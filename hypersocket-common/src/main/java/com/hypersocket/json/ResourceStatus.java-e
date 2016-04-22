/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="resourceStatus")
public class ResourceStatus<T> {

	boolean success = true;
	String message = "";
	T resource;
	
	public ResourceStatus() {
		super();
	}

	public ResourceStatus(T resource, String message) {
		this.resource = resource;
		this.message = message;
	}
	
	public ResourceStatus(T resource) {
		this.resource = resource;
	}
	
	public ResourceStatus(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public ResourceStatus(boolean success, T resource, String message) {
		this.success = success;
		this.resource = resource;
		this.message = message;
	}
	
	public ResourceStatus(boolean success) {
		this(success, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}

	
}
