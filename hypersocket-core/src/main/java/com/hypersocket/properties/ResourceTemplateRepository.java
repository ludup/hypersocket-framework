package com.hypersocket.properties;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.hypersocket.resource.AbstractResource;

public interface ResourceTemplateRepository extends PropertyRepository {

	void loadPropertyTemplates(String xmlResourcePath);
	
	String getValue(AbstractResource resource, String resourceKey);

	Integer getIntValue(AbstractResource resource, String resourceKey) throws NumberFormatException;

	Boolean getBooleanValue(AbstractResource resource, String resourceKey);

	Long getLongValue(AbstractResource resource, String name)
			throws NumberFormatException;
	
	void setValue(AbstractResource resource, String resourceKey, String value);

	void setValue(AbstractResource resource, String resourceKey, Integer value);

	void setValue(AbstractResource resource, String resourceKey, Long value);
	
	void setValue(AbstractResource resource, String name, Boolean value);

	void setValue(AbstractResource resource, String name, Date value);
	
	Collection<PropertyCategory> getPropertyCategories(AbstractResource resource, PropertyFilter... filters);
	
	String[] getValues(AbstractResource resource, String name);

	Collection<PropertyTemplate> getPropertyTemplates(AbstractResource resource);

	Collection<PropertyCategory> getPropertyCategories(
			AbstractResource resource, String group);

	Set<String> getPropertyNames(AbstractResource resource);
	
	Map<String,String> getProperties(AbstractResource resource);

	PropertyTemplate getPropertyTemplate(AbstractResource resource, String resourceKey);

	boolean hasPropertyTemplate(AbstractResource resource, String key);

	boolean hasPropertyValueSet(AbstractResource resource, String resourceKey);
	
	Set<String> getVariableNames(AbstractResource resource);

	void setValues(AbstractResource resource,
			Map<String, String> properties);

	Date getDateValue(AbstractResource resource, String name)
			throws NumberFormatException;

	void registerPropertyResolver(PropertyResolver resolver);

	ResourcePropertyStore getDatabasePropertyStore();

	Double getDoubleValue(AbstractResource resource, String resourceKey);

	void setDoubleValue(AbstractResource resource, String resourceKey,
			Double value);

	Set<String> getPropertyNames(AbstractResource resource,
			boolean includeResolvers);

	String getValue(AbstractResource resource, String resourceKey, String defaultValue);

	Map<String, PropertyTemplate> getRepositoryTemplates();

	String getDecryptedValue(AbstractResource resource, String resourceKey);

	Long[] getLongValues(AbstractResource resource, String name);

}
