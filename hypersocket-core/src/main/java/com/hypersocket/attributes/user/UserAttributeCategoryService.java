package com.hypersocket.attributes.user;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface UserAttributeCategoryService extends AbstractResourceService<UserAttributeCategory> {

	
	public UserAttributeCategory createAttributeCategory(String name,
			int weight) throws ResourceCreationException,
			AccessDeniedException;

	public UserAttributeCategory updateAttributeCategory(
			UserAttributeCategory category, String name,
			int weight) throws AccessDeniedException, ResourceChangeException;

	public void deleteAttributeCategory(UserAttributeCategory category)
			throws AccessDeniedException, ResourceChangeException;

	public Collection<String> getContexts();

	public Long getMaximumCategoryWeight() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyCategories();

	PropertyCategory getCategoryByResourceKey(String resourceKey);

	PropertyCategory registerPropertyCategory(UserAttributeCategory c);


}
