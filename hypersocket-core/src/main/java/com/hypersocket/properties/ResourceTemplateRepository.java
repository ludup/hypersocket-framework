package com.hypersocket.properties;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.hypersocket.resource.SimpleResource;

public interface ResourceTemplateRepository extends PropertyRepository {

	default void loadPropertyTemplates(String xmlResourcePath) {
		loadPropertyTemplates(xmlResourcePath, Thread.currentThread().getContextClassLoader() == null ? ResourceTemplateRepository.class.getClassLoader() : Thread.currentThread().getContextClassLoader());
	}

	void loadPropertyTemplates(String xmlResourcePath, ClassLoader classLoader);

	void unloadPropertyTemplates(ClassLoader classLoader);
	
	default void unloadPropertyTemplates() {
		unloadPropertyTemplates(Thread.currentThread().getContextClassLoader() == null ? ResourceTemplateRepository.class.getClassLoader() : Thread.currentThread().getContextClassLoader());
	}
	
	String getValue(SimpleResource resource, String resourceKey);

	Integer getIntValue(SimpleResource resource, String resourceKey) throws NumberFormatException;

	Boolean getBooleanValue(SimpleResource resource, String resourceKey);

	Long getLongValue(SimpleResource resource, String name)
			throws NumberFormatException;
	
	void setValue(SimpleResource resource, String resourceKey, String value);

	void setValue(SimpleResource resource, String resourceKey, Integer value);

	void setValue(SimpleResource resource, String resourceKey, Long value);
	
	void setValue(SimpleResource resource, String name, Boolean value);

	void setValue(SimpleResource resource, String name, Date value);
	
	Collection<PropertyCategory> getPropertyCategories(SimpleResource resource, PropertyFilter... filters);
	
	String[] getValues(SimpleResource resource, String name);

	Collection<PropertyTemplate> getPropertyTemplates(SimpleResource resource);

	Collection<PropertyCategory> getPropertyCategories(
			SimpleResource resource, String group);

	Set<String> getPropertyNames(SimpleResource resource);
	
	Map<String,String> getProperties(SimpleResource resource);

	PropertyTemplate getPropertyTemplate(SimpleResource resource, String resourceKey);

	boolean hasPropertyTemplate(SimpleResource resource, String key);

	boolean hasPropertyValueSet(SimpleResource resource, String resourceKey);
	
	Set<String> getVariableNames(SimpleResource resource);

	void setValues(SimpleResource resource,
			Map<String, String> properties);

	Date getDateValue(SimpleResource resource, String name)
			throws NumberFormatException;

	void registerPropertyResolver(PropertyResolver resolver);

	ResourcePropertyStore getDatabasePropertyStore();

	Double getDoubleValue(SimpleResource resource, String resourceKey);

	void setDoubleValue(SimpleResource resource, String resourceKey,
			Double value);

	Set<String> getPropertyNames(SimpleResource resource,
			boolean includeResolvers);

	String getValue(SimpleResource resource, String resourceKey, String defaultValue);

	Map<String, PropertyTemplate> getRepositoryTemplates();

	String getDecryptedValue(SimpleResource resource, String resourceKey);

	Long[] getLongValues(SimpleResource resource, String name);

	Integer[] getIntValues(SimpleResource resource, String name);
	
	Map<String, String> getProperties(SimpleResource resource, boolean decrypt);

	String getValueOrDefault(SimpleResource resource, String resourceKey, String defaultValue);

	Boolean getBooleanValueOrDefault(SimpleResource resource, String name, Boolean defaultValue);

	Integer getIntValueOrDefault(SimpleResource resource, String name, Integer defaultValue)
			throws NumberFormatException;

}
