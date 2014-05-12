/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.util.ArrayList;
import java.util.List;

import com.hypersocket.json.JsonPrincipal;

public class PrincipalResourcesWrapper {

	List<JsonPrincipal> resources = new ArrayList<JsonPrincipal>();
	
	public PrincipalResourcesWrapper() {
		
	}

	public List<JsonPrincipal> getResources() {
		return resources;
	}

	public void setResources(List<JsonPrincipal> resources) {
		this.resources = resources;
	}
	
	
}
