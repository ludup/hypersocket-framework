package com.hypersocket.properties;

import java.util.Collection;
import java.util.Map;

public interface PropertyTemplateRepository {

	void loadPropertyTemplates(String string);

	String getValue(String resourceKey);

	Integer getIntValue(String name) throws NumberFormatException;

	Boolean getBooleanValue(String name);

	void setValue(String resourceKey, String value);

	void setValue(String resourceKey, Integer value);

	void setValue(String name, Boolean value);

	Collection<PropertyCategory> getPropertyCategories();
	
	Collection<PropertyCategory> getPropertyCategories(String group);

	String[] getValues(String name);
	
	void setValues(Map<String,String> values);

	PropertyTemplate getPropertyTemplate(String resourceKey);

}
