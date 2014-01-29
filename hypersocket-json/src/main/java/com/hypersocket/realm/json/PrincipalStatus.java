/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.json.StatusTemplate;
import com.hypersocket.realm.Principal;

@XmlRootElement(name="principalStatus")
public class PrincipalStatus extends StatusTemplate<Long> {

	Principal principal;
	
	public PrincipalStatus() {
	}

	public PrincipalStatus(boolean success, String message) {
		super(success, message);
	}
	
	public PrincipalStatus(boolean success, String message, Principal principal) {
		super(success, message, principal.getId());
		this.principal = principal;
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

}
