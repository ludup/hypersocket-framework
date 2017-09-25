package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.SimpleResource;

public interface PropertyResolver {

	Collection<String> getPropertyNames(SimpleResource resource);

	Collection<String> getVariableNames(SimpleResource resource);

	PropertyTemplate getPropertyTemplate(SimpleResource resource,
			String resourceKey);

	Collection<PropertyCategory> getPropertyCategories(
			SimpleResource resource);

	Collection<PropertyTemplate> getPropertyTemplates(
			SimpleResource resource);

	boolean hasPropertyTemplate(SimpleResource resource, String resourceKey);

}
