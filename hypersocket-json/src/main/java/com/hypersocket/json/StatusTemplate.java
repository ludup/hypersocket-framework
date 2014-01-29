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
public class StatusTemplate<T> extends RequestStatus {

	T id;
	
	public StatusTemplate() {
		super();
	}

	public StatusTemplate(boolean success, String message, T id) {
		super(success, message);
		this.id = id;
	}

	public StatusTemplate(boolean success, String message) {
		super(success, message);
	}
	
	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}
	
	

}
