/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.input;

public class CountryField extends InputField {


	public CountryField(String resourceKey, String defaultValue, boolean required, String label) {
		super(InputFieldType.countries, resourceKey, defaultValue, required, label);
	}
}
