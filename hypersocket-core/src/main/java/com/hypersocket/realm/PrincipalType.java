/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

public enum PrincipalType {

	USER,
	GROUP,
	SERVICE,
	SYSTEM,
	TEMPLATE;
	
	public static final PrincipalType[] ALL_TYPES = { USER, GROUP, SERVICE, SYSTEM };
	
}
