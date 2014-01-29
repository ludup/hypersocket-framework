package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.Resource;

public interface ResourceTemplateRepository {

	void loadPropertyTemplates(String xmlResourcePath);
	
	String getValue(Resource resource, String resourceKey);

	Integer getIntValue(Resource resource, String resourceKey) throws NumberFormatException;

	Boolean getBooleanValue(Resource resource, String resourceKey);

	void setValue(Resource resource, String resourceKey, String value);

	void setValue(Resource resource, String resourceKey, Integer value);

	void setValue(Resource resource, String name, Boolean value);

	Collection<PropertyCategory> getPropertyCategories(Resource resource);

	String[] getValues(Resource resource, String name);

}
