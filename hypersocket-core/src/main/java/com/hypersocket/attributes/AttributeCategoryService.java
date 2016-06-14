package com.hypersocket.attributes;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface AttributeCategoryService<A extends AbstractAttribute<T>, T extends RealmAttributeCategory<A>> extends AbstractResourceService<T> {

	
	public T createAttributeCategory(String name,
			int weight) throws ResourceCreationException,
			AccessDeniedException;

	public T updateAttributeCategory(
			T category, String name,
			int weight) throws AccessDeniedException, ResourceChangeException;

	public void deleteAttributeCategory(T category)
			throws AccessDeniedException, ResourceChangeException;

	public Collection<String> getContexts();

	public Long getMaximumCategoryWeight() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyCategories();

	PropertyCategory getCategoryByResourceKey(String resourceKey);

	PropertyCategory registerPropertyCategory(T c);


}
