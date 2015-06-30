package com.hypersocket.attributes;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface AttributeService extends AuthenticatedService {

	Long getMaximumCategoryWeight() throws AccessDeniedException;

	AttributeCategory getCategoryByName(String name)
			throws ResourceNotFoundException, AccessDeniedException;

	Long getMaximumAttributeWeight(AttributeCategory cat)
			throws AccessDeniedException;

	AttributeCategory createAttributeCategory(String name, String context,
			int weight) throws ResourceCreationException, AccessDeniedException;

	UserAttribute getAttributeByName(String name)
			throws ResourceNotFoundException, AccessDeniedException;

	UserAttribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException, AccessDeniedException;

}
