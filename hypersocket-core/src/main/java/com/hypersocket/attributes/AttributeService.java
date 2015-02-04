package com.hypersocket.attributes;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
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

	public Attribute updateAttribute(Attribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, Boolean readOnly, Boolean encrypted,
			String variableName) throws ResourceCreationException,
			AccessDeniedException;

	public Attribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException, AccessDeniedException;

	public void deleteAttribute(Attribute attribute)
			throws AccessDeniedException;

	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException;

	public Long getAttributeCount(String searchPattern)
			throws AccessDeniedException;
}
