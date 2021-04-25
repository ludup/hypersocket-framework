/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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

	private InputFieldType type;
	private String resourceKey;
	private String defaultValue;
	private boolean required;
	private String label;
	private List<Option> options = new ArrayList<Option>();
	private String infoKey;
	private String onChange;
	private boolean readOnly;
	private String classes;
	private String help;
	
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

	protected InputField(InputFieldType type, String resourceKey,
						 String defaultValue, boolean required, String label, String infoKey) {
		this(type, resourceKey, defaultValue, required, label);
		this.infoKey = infoKey;
	}

	public String getLabel() {
		return label;
	}

	public InputField setLabel(String label) {
		this.label = label;
		return this;
	}

	@XmlElement(name = "option")
	public List<Option> getOptions() {
		return options;
	}

	public InputField addOption(Option option) {
		options.add(option);
		return this;
	}

	public InputField setOptions(List<Option> options) {
		this.options = options;
		return this;
	}

	public InputFieldType getType() {
		return type;
	}

	public InputField setType(InputFieldType type) {
		this.type = type;
		return this;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public InputField setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public InputField setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public InputField setRequired(boolean required) {
		this.required = required;
		return this;
	}

	public String getInfoKey() {
		return infoKey;
	}

	public InputField setInfoKey(String infoKey) {
		this.infoKey = infoKey;
		return this;
	}

	public String getOnChange() {
		return onChange;
	}

	public InputField setOnChange(String onclick) {
		this.onChange = onclick;
		return this;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public InputField setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		return this;
	}

	public String getClasses() {
		return classes;
	}

	public InputField setClasses(String classes) {
		this.classes = classes;
		return this;
	}

	public String getHelp() {
		return help;
	}

	public InputField setHelp(String help) {
		this.help = help;
		return this;
	}
	
	
	
	
}
