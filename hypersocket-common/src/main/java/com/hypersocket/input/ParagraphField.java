/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;

public class ParagraphField extends InputField {

	private boolean isValueResourceKey;
	private boolean isAlert;
	private String alertType = "danger";
	
	public ParagraphField(String defaultValue, boolean isValueResourceKey) {
		super(InputFieldType.p, "paragraphField", defaultValue, false, "");
		this.isValueResourceKey = isValueResourceKey;
	}
	
	public ParagraphField(String defaultValue, boolean isValueResourceKey, boolean isAlert, String alertType) {
		super(InputFieldType.p, "paragraphField", defaultValue, false, "");
		this.isValueResourceKey = isValueResourceKey;
		this.isAlert = isAlert;
		this.alertType = alertType;
	}
	
	public boolean isValueResourceKey() {
		return isValueResourceKey;
	}

	public boolean isAlert() {
		return isAlert;
	}

	public String getAlertType() {
		return alertType;
	}
	
	
}
