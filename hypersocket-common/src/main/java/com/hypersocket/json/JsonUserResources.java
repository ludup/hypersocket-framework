/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonUserResources {
	JsonUserAction[] resources;

	public JsonUserAction[] getResources() {
		return resources;
	}

	public void setResources(JsonUserAction[] resources) {
		this.resources = resources;
	}
	
	
     
}
