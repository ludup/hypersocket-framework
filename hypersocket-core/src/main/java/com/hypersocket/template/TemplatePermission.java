/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.template;

import com.hypersocket.permissions.PermissionType;

public enum TemplatePermission implements PermissionType {
	
	CREATE("template.create"),
	READ("template.read"),
	UPDATE("template.update"),
	DELETE("template.delete");
	
	private final String val;
	
	private TemplatePermission(final String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}

	@Override
	public String getResourceKey() {
		return val;
	}
}
