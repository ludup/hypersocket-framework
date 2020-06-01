/**
 * Copyright 2003-2016 SSHTOOLS Limited. All Rights Reserved.
 *
 * For product documentation visit https://www.sshtools.com/
 *
 * This file is part of Hypersocket JSON Client.
 *
 * Hypersocket JSON Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypersocket JSON Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hypersocket JSON Client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hypersocket.tests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonPrincipal;
import com.hypersocket.json.JsonResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSession {

	String id;
	String csrfToken;
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

	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}
	
	
}
