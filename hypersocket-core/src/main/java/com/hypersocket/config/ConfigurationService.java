/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config;

import java.util.Locale;
import java.util.Map;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.PropertyTemplateService;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;


public interface ConfigurationService extends AuthenticatedService, PropertyTemplateService {

	public static final String RESOURCE_BUNDLE = "ConfigurationService";

	void setValues(Map<String, String> values) throws AccessDeniedException, ResourceException;

	String getValue(Realm realm, String resourceKey);

	Boolean getBooleanValue(Realm realm, String name);

	Integer getIntValue(Realm realm, String name) throws NumberFormatException;

	String[] getValues(Realm realm, String string);

	void setValue(Realm realm, String resourceKey, String value)
			throws AccessDeniedException, ResourceException;

	Double getDoubleValue(Realm realm, String name);

	void setDoubleValue(Realm realm, String name, Double value);

	boolean hasUserLocales();

	Locale getDefaultLocale();

	String getDecryptedValue(Realm realm, String resourceKey);

	void setValues(Realm realm, String string, String[] editable) throws ResourceException, AccessDeniedException;

	PropertyTemplate getPropertyTemplate(Realm realm, String string);

	Double getDoubleValue(String name);

	Long getLongValue(Realm realm, String val);
	
	Long getLongValue(String val);

	String getValueOrSystemDefault(Realm realm, String resourceKey);

	Boolean getBooleanValueOrSystemDefault(Realm realm, String string);

	Integer getIntValueOrSystemDefault(Realm realm, String string);

	void deleteRealm(Realm realm);

	void resetCache(Realm realm);

}
