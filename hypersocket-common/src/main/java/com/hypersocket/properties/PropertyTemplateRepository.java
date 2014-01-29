package com.hypersocket.properties;

import java.util.Collection;

public interface PropertyTemplateRepository {

	void loadPropertyTemplates(String string);

	String getValue(String resourceKey);

	Integer getIntValue(String name) throws NumberFormatException;

	Boolean getBooleanValue(String name);

	void setValue(String resourceKey, String value);

	void setValue(String resourceKey, Integer value);

	void setValue(String name, Boolean value);

	Collection<PropertyCategory> getPropertyCategories();

	String[] getValues(String name);

}
