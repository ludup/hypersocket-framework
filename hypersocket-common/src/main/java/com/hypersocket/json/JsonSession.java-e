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
public class JsonSession {

	String id;
	JsonPrincipal currentPrincipal;
	JsonResource currentRealm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JsonPrincipal getCurrentPrincipal() {
		return currentPrincipal;
	}

	public void setPrincipal(JsonPrincipal currentPrincipal) {
		this.currentPrincipal = currentPrincipal;
	}

	public JsonResource getCurrentRealm() {
		return currentRealm;
	}

	public void setCurrentRealm(JsonResource currentRealm) {
		this.currentRealm = currentRealm;
	}
}
