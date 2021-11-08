/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;

public class HeaderField extends InputField {

	private boolean isValueResourceKey;
	
	public enum Type { h1, h2, h3, h4, h5, h6 };
	
	Type type;
	public HeaderField(String defaultValue, Type type, boolean valueIsResourceKey) {
		super(InputFieldType.header, "headerField", defaultValue, false, "");
		this.isValueResourceKey = valueIsResourceKey;
		this.type = type;
	}
	
	public boolean isValueResourceKey() {
		return isValueResourceKey;
	}

	public String getTag() {
		return type.name();
	}
	
}
