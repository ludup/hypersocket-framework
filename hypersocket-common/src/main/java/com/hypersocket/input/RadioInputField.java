/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;


public class RadioInputField extends InputField {

	public RadioInputField() {	
	}
	
	public RadioInputField(String resourceKey, String defaultValue, boolean required, String label) {
		super(InputFieldType.radio, resourceKey, defaultValue, required, label);
	}

}
