package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hypersocket.resource.AbstractResource;

public class DatabasePropertyStore implements ResourcePropertyStore {

	PropertyRepository repository;

	Map<String, String> cachedValues = new HashMap<String, String>();
	Map<String, PropertyTemplate> templates = new HashMap<String, PropertyTemplate>();
	Map<String, List<PropertyTemplate>> templatesByModule = new HashMap<String, List<PropertyTemplate>>();

	public DatabasePropertyStore(PropertyRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setProperty(PropertyTemplate template, String value) {

		DatabaseProperty property = repository.getProperty(template
				.getResourceKey());
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(template.getResourceKey());
		}
		property.setValue(value);
		repository.saveProperty(property);
		cachedValues.remove(property.getResourceKey());
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {

		String c = null;
		if (!cachedValues.containsKey(template.getResourceKey())) {
			Property p = repository.getProperty(template.getResourceKey());
			if (p == null) {
				c = template.getDefaultValue();
			} else {
				c = p.getValue();
			}
			cachedValues.put(template.getResourceKey(), c);
		} else {
			c = cachedValues.get(template.getResourceKey());
		}

		return c;
	}

	private String createCacheKey(String resourceKey, AbstractResource resource) {
		String key = resourceKey;
		if (resource != null) {
			key += "/" + resource.getId();
		}
		return key;
	}

	@Override
	public List<Property> getProperties(String resourceXmlPath) {
		// This is a little inefficient but ensures we only get registered
		// properties
		List<Property> properties = new ArrayList<Property>();
		for (PropertyTemplate t : templatesByModule.get(resourceXmlPath)) {
			Property p = repository.getProperty(t.getResourceKey());
			if (p != null) {
				properties.add(p);
			}
		}
		return properties;
	}

	@Override
	public void registerTemplate(PropertyTemplate template, String module) {
		templates.put(template.getResourceKey(), template);
		if (!templatesByModule.containsKey(module)) {
			templatesByModule.put(module, new ArrayList<PropertyTemplate>());
		}
		templatesByModule.get(module).add(template);
	}

	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return templates.get(resourceKey);
	}

	@Override
	public String getPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		String c;
		String cacheKey = createCacheKey(template.getResourceKey(), resource);
		if (!cachedValues.containsKey(cacheKey)) {
			Property p = repository.getProperty(template.getResourceKey(), resource);
			if (p == null) {
				c = template.getDefaultValue();
			} else {
				c = p.getValue();
			}
			cachedValues.put(cacheKey, c);
		} else {
			c = cachedValues.get(cacheKey);
		}

		return c;
	}

	@Override
	public void setPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {

		DatabaseProperty property = repository.getProperty(
				template.getResourceKey(), resource);
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(template.getResourceKey());
			property.setResource(resource);
		}
		property.setValue(value);
		repository.saveProperty(property);
		cachedValues
				.remove(createCacheKey(template.getResourceKey(), resource));

	}

}
