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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FormTemplate {

	protected String resourceKey;
	protected String scheme;
	protected List<InputField> fields = new ArrayList<InputField>();
	protected boolean showLogonButton = true;
	protected boolean showStartAgain = true;
	protected String logonButtonResourceKey = null;
	protected String logonButtonIcon = null;
	protected String formClass = null;
	
	public FormTemplate(String scheme) {
		this.scheme = scheme;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getScheme() {
		return scheme;
	}
	
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

	public String getLogonButtonResourceKey() {
		return logonButtonResourceKey;
	}

	public void setLogonButtonResourceKey(String logonButtonResourceKey) {
		this.logonButtonResourceKey = logonButtonResourceKey;
	}

	public String getLogonButtonIcon() {
		return logonButtonIcon;
	}

	public void setLogonButtonIcon(String logonButtonIcon) {
		this.logonButtonIcon = logonButtonIcon;
	}

	public boolean isShowStartAgain() {
		return showStartAgain;
	}

	public void setShowStartAgain(boolean showStartAgain) {
		this.showStartAgain = showStartAgain;
	}

}
