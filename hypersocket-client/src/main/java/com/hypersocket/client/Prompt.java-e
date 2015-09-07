/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hypersocket.client.Option;
import com.hypersocket.client.i18n.I18N;

public class Prompt implements Serializable {

	private static final long serialVersionUID = 6832904649822127752L;

	private String resourceKey;
	private String defaultValue;
	private PromptType type;
	private List<Option> options = new ArrayList<Option>();
	
	public Prompt(PromptType type, String resourceKey, String defaultValue) {
		this.type = type;
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
	}
	
	public PromptType getType() {
		return type;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public String getLabel() {
		return I18N.getResource(resourceKey + ".label");
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void addOption(Option option) {
		options.add(option);
	}
	
	public List<Option> getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return "Prompt [resourceKey=" + resourceKey + ", defaultValue="
				+ defaultValue + ", type=" + type + ", options=" + options
				+ "]";
	}
	
}
