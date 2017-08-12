package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface PropertyTemplateService {

	String getValue(String resourceKey);

	Integer getIntValue(String name) throws NumberFormatException;

	Boolean getBooleanValue(String name);

	void setValue(String resourceKey, String value)
			throws AccessDeniedException, ResourceException;

	void setValue(String resourceKey, Integer value)
			throws AccessDeniedException, ResourceException;

	void setValue(String name, Boolean value) throws AccessDeniedException, ResourceException;

	void setValues(String resourceKey, String[] add) throws ResourceException, AccessDeniedException;
	
	Collection<PropertyCategory> getPropertyCategories()
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException;
	
	String[] getValues(String name);

}
