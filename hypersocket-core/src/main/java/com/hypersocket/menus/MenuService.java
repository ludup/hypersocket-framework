/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.menus;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;

public interface MenuService extends AuthenticatedService {

	final static String RESOURCE_BUNDLE = "MenuService";
	
	static final String CONFIGURATION_MODULE = "configuration";
	static final String ACCESS_CONTROL_MODULE = "accessControl";

	static final String MENU_MY_RESOURCES = "myResources";
	static final String MENU_MY_PROFILE = "profile";
	
	static final String MENU_SECURITY = "security";
	static final String MENU_ACCESS_CONTROL = "accessControl";
	
	static final String MENU_SYSTEM = "system";
	static final String MENU_CONFIGURATION = "configuration";
	
	static final String MENU_RESOURCES = "resources";

	
	List<Menu> getMenus();

	boolean registerMenu(MenuRegistration module);

	boolean registerMenu(MenuRegistration module, String parentModule);
}
