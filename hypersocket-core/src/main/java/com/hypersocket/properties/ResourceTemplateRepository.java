package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.AbstractResource;

public interface ResourceTemplateRepository {

	void loadPropertyTemplates(String xmlResourcePath);
	
	String getValue(AbstractResource resource, String resourceKey);

	Integer getIntValue(AbstractResource resource, String resourceKey) throws NumberFormatException;

	Boolean getBooleanValue(AbstractResource resource, String resourceKey);

	void setValue(AbstractResource resource, String resourceKey, String value);

	void setValue(AbstractResource resource, String resourceKey, Integer value);

	void setValue(AbstractResource resource, String name, Boolean value);

	Collection<PropertyCategory> getPropertyCategories(AbstractResource resource);

	String[] explodeValues(String values);
	
	String[] getValues(AbstractResource resource, String name);

}
