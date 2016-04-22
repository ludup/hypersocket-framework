/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

public class AuthenticationUtils {

	@SuppressWarnings("rawtypes")
	public static String getRequestParameter(Map parameters, String name) {
		
		if(parameters.containsKey(name)) {
			Object obj = parameters.get(name);
			if(obj instanceof String) {
				return (String)obj;
			} else if(obj instanceof String[]) {
				String[] arr = (String[])obj;
				if(arr.length > 0) {
					return arr[0];
				}
			}
		} 
	
		return null;
		
	}
}
