/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import java.util.List;

import com.hypersocket.resource.Resource;

public interface PropertyRepository {

	DatabaseProperty getProperty(String resourceKey);
	
	DatabaseProperty getProperty(String resourceKey, Resource resource);
	
	Property getProperty(Long id);

	void saveProperty(DatabaseProperty resourceKey);
	
	List<DatabaseProperty> getPropertiesWithValue(String resourceKey,
			String value);

	List<DatabaseProperty> getPropertiesForResource(Resource resource);

	void deletePropertiesForResource(Resource resource);

}
