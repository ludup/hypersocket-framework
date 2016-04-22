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

@XmlRootElement(name="credentialStatus")
public class CredentialsStatus extends StatusTemplate<Long> {

	public CredentialsStatus() {
		super();
	}

	public CredentialsStatus(boolean success, String message, Long id) {
		super(success, message, id);
	}

	public CredentialsStatus(boolean success, String message) {
		super(success, message);
	}

}
