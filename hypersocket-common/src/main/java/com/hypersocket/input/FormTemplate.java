/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FormTemplate {

	protected String resourceKey;
	protected List<InputField> fields = new ArrayList<InputField>();
	protected boolean showLogonButton = true;
	protected String formClass = null;
	
	public FormTemplate() {
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	@XmlElement(name="inputField")
	public List<InputField> getInputFields() {
		return fields;
	}

	public void setInputFields(List<InputField> fields) {
		this.fields = fields;
	}
	
	public boolean isShowLogonButton() {
		return showLogonButton;
	}
	
	public void setShowLogonButton(boolean showLogonButton) {
		this.showLogonButton = showLogonButton;
	}
	
	public String getFormClass() {
		return formClass;
	}
	
	public void setFormClass(String formClass) {
		this.formClass = formClass;
	}
	
}
