package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.AbstractResource;

public interface PropertyResolver {

	Collection<String> getPropertyNames(AbstractResource resource);

	Collection<String> getVariableNames(AbstractResource resource);

	PropertyTemplate getPropertyTemplate(AbstractResource resource,
			String resourceKey);

	Collection<PropertyCategory> getPropertyCategories(
			AbstractResource resource);

	Collection<PropertyTemplate> getPropertyTemplates(
			AbstractResource resource);

	boolean hasPropertyTemplate(AbstractResource resource, String resourceKey);

}
