package com.hypersocket.attributes;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;

public interface AttributeCategoryService<A extends AbstractAttribute<T>, T extends RealmAttributeCategory<A>> extends AbstractResourceService<T> {

	
	public T createAttributeCategory(String name,
			int weight) throws ResourceException,
			AccessDeniedException;

	public T updateAttributeCategory(
			T category, String name,
			int weight) throws AccessDeniedException, ResourceException;

	public void deleteAttributeCategory(T category)
			throws AccessDeniedException, ResourceException;

	public Collection<String> getContexts();

	public Long getMaximumCategoryWeight() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyCategories();

	PropertyCategory getCategoryByResourceKey(String resourceKey);

	PropertyCategory registerPropertyCategory(T c);


}
