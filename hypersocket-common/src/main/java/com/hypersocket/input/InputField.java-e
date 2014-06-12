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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "inputField")
public class InputField {

	InputFieldType type;
	String resourceKey;
	String defaultValue;
	boolean required;
	String label;
	List<Option> options = new ArrayList<Option>();

	public InputField() {

	}

	protected InputField(InputFieldType type, String resourceKey,
			String defaultValue, boolean required, String label) {
		this.type = type;
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
		this.required = required;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlElement(name = "option")
	public List<Option> getOptions() {
		return options;
	}

	public void addOption(Option option) {
		options.add(option);
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public InputFieldType getType() {
		return type;
	}

	public void setType(InputFieldType type) {
		this.type = type;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

}
