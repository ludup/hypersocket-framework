package com.hypersocket.attributes;

import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.attributes.user.UserAttributeCategoryService;
import com.hypersocket.attributes.user.UserAttributeService;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

@Service
public class AttributeFacadeServiceImpl extends AbstractAuthenticatedServiceImpl
		implements AttributeFacadeService {

	@Autowired
	private UserAttributeCategoryService categoryService;

	@Autowired
	private UserAttributeService attributeService;

	/**
	 * Following methods are for backwards compatibility with old attributes
	 */
	@Override
	public Long getMaximumCategoryWeight() throws AccessDeniedException {
		return categoryService.getMaximumCategoryWeight();
	}

	@Override
	public Long getMaximumAttributeWeight(AttributeCategory<?> cat)
			throws AccessDeniedException {
		return attributeService
				.getMaximumAttributeWeight((UserAttributeCategory) cat);
	}

	@Override
	public AttributeCategory<?> getCategoryByName(String name)
			throws ResourceNotFoundException, AccessDeniedException {
		try {
			return categoryService.getResourceByName(name, getCurrentRealm());
		} catch (ResourceNotFoundException e) {
			return null;
		}
	}

	@Override
	public AttributeCategory<?> createAttributeCategory(String name,
			String context, int weight) throws ResourceException,
			AccessDeniedException {
		return categoryService.createAttributeCategory(name, weight);
	}

	@Override
	public UserAttribute getAttributeByName(String name)
			throws ResourceNotFoundException, AccessDeniedException {
		try {
			return attributeService.getResourceByName(name, getCurrentRealm());
		} catch (ResourceNotFoundException e) {
			return null;
		}
	}

	@Override
	public UserAttribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean required, Boolean encrypted, String variableName)
			throws ResourceException, AccessDeniedException {

		return attributeService.createAttribute(name, category, description,
				defaultValue, weight, type, readOnly ? "admin" : "", false, required, encrypted, variableName,
				new HashSet<Role>(), new ArrayList<NameValuePair>());
	}
}
