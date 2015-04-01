package com.hypersocket.attributes;

import java.util.Collection;
import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.tables.ColumnSort;

public interface AttributeService extends AuthenticatedService {

	public List<AttributeCategory> getCategories() throws AccessDeniedException;

	public AttributeCategory getAttributeCategoryById(Long id)
			throws AccessDeniedException;

	public Attribute getAttributeById(Long id) throws AccessDeniedException;

	public AttributeCategory createAttributeCategory(String name,
			String context, int weight) throws ResourceCreationException,
			AccessDeniedException;

	public AttributeCategory updateAttributeCategory(
			AttributeCategory category, Long id, String name, String context,
			int weight) throws AccessDeniedException, ResourceChangeException;

	public Attribute updateAttribute(Attribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, Boolean readOnly, Boolean encrypted,
			String variableName) throws AccessDeniedException,
			ResourceChangeException;

	public Attribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException, AccessDeniedException;

	public void deleteAttribute(Attribute attribute)
			throws AccessDeniedException;

	public void deleteAttributeCategory(AttributeCategory category)
			throws AccessDeniedException;

	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException;

	public List<AttributeCategory> searchAttributeCategories(
			String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException;

	public Long getAttributeCount(String searchPattern)
			throws AccessDeniedException;

	public Long getAttributeCategoryCount(String searchPattern)
			throws AccessDeniedException;

	public Collection<String> getContexts();

	public AttributeCategory getCategoryByName(String attributeCategory) throws AccessDeniedException;

	public Long getMaximumCategoryWeight() throws AccessDeniedException;

	public Long getMaximumAttributeWeight(AttributeCategory cat) throws AccessDeniedException;
}
