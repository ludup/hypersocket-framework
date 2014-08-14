/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.hypersocket.json.JsonPrincipal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSession {
	
	String id;
	JsonPrincipal principal;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public JsonPrincipal getPrincipal() {
		return principal;
	}
	public void setPrincipal(JsonPrincipal principal) {
		this.principal = principal;
	}
	
	
   
}
