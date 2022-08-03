/*******************************************************************************
 * Copyright (c) 2019 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.hypersocket.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface PluginResourceService {

	public static final String RESOURCE_BUNDLE = "PluginResourceService";

	List<PluginResource> searchResources(Realm currentRealm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException;

	Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern) throws AccessDeniedException;

	Collection<PluginResource> getResources(Realm currentRealm) throws AccessDeniedException, IOException;

	PluginResource getResourceById(String id) throws AccessDeniedException, IOException;

	void deleteResource(PluginResource resource) throws AccessDeniedException, IOException;

	PluginResource stop(PluginResource resource) throws AccessDeniedException, IOException;

	List<PluginResource> getResourcesByIds(String[] ids) throws AccessDeniedException, IOException;

	void deleteResources(List<PluginResource> messageResources) throws AccessDeniedException, IOException;

	Collection<PropertyCategory> getPropertyTemplate(PluginResource resource);

	Collection<PropertyCategory> getPropertyTemplate();

	PluginResource updateResource(Realm realm, PluginResource resourceById, Map<String, String> properties) throws AccessDeniedException, IOException;

	PluginResource createResource(Realm realm, Map<String, String> properties) throws IOException, AccessDeniedException;

	PluginResource start(PluginResource resource) throws AccessDeniedException, IOException;

	PluginResource enable(PluginResource resource) throws AccessDeniedException, IOException;

	PluginResource disable(PluginResource resource) throws AccessDeniedException, IOException;

	void uninstall(PluginResource resourceById, boolean deleteData) throws Exception;

	PluginResource upload(Realm realm, InputStream inputStream, boolean start) throws IOException, AccessDeniedException;

}
