/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config;

import java.util.Map;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyTemplateService;
import com.hypersocket.resource.ResourceChangeException;


public interface SystemConfigurationService extends AuthenticatedService, PropertyTemplateService {

	public static final String RESOURCE_BUNDLE = "ConfigurationService";

	void setValues(Map<String, String> values) throws AccessDeniedException, ResourceChangeException;

}
