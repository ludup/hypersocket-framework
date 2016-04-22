package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceChangeException;

public interface PropertyTemplateService {

	String getValue(String resourceKey);

	Integer getIntValue(String name) throws NumberFormatException;

	Boolean getBooleanValue(String name);

	void setValue(String resourceKey, String value)
			throws AccessDeniedException, ResourceChangeException;

	void setValue(String resourceKey, Integer value)
			throws AccessDeniedException, ResourceChangeException;

	void setValue(String name, Boolean value) throws AccessDeniedException, ResourceChangeException;

	void setValues(String resourceKey, String[] add) throws ResourceChangeException, AccessDeniedException;
	
	Collection<PropertyCategory> getPropertyCategories()
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException;
	
	String[] getValues(String name);

}
