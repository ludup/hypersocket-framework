/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;

public class ParagraphField extends InputField {

	boolean isValueResourceKey;
	
	public ParagraphField(String defaultValue, boolean isValueResourceKey) {
		super(InputFieldType.p, "paragraphField", defaultValue, false, "");
		this.isValueResourceKey = isValueResourceKey;
	}
	
	public boolean isValueResourceKey() {
		return isValueResourceKey;
	}
}
