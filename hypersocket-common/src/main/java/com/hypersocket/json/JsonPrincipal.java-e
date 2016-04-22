/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonPrincipal {

	String principalName;
	String principalDesc;
	Long id;
	JsonPrincipalType type;
	
	List<JsonPrincipal> groups = new ArrayList<JsonPrincipal>();
	
	public JsonPrincipal() {
		
	}
	public String getPrincipalName() {
		return principalName;
	}
	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}
	public String getPrincipalDesc() {
		return principalDesc;
	}
	public void setPrincipalDesc(String principalDesc) {
		this.principalDesc = principalDesc;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<JsonPrincipal> getGroups() {
		return groups;
	}
	public void setGroups(List<JsonPrincipal> groups) {
		this.groups = groups;
	}
	public void setType(JsonPrincipalType type) {
		this.type = type;
	}
	public JsonPrincipalType getType() {
		return type;
	}
	
	
}
