/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import com.hypersocket.input.FormTemplate;

public class AuthenticationRequiredResult extends AuthenticationResult {

	FormTemplate formTemplate;
	boolean lastErrorIsResourceKey;
	
	public AuthenticationRequiredResult() {
		
	}
	
	public AuthenticationRequiredResult(String bannerMsg, String errorMsg, boolean lastErrorIsResourceKey, FormTemplate formTemplate, boolean showLocales) {
		super(bannerMsg, errorMsg, showLocales);
		this.formTemplate = formTemplate;
		this.lastErrorIsResourceKey = lastErrorIsResourceKey;
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	public void setFormTemplate(FormTemplate template) {
		this.formTemplate = template;
	}
	
	public boolean getLastErrorIsResourceKey() {
		return lastErrorIsResourceKey;
	}
	
	
}
