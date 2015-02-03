package com.hypersocket.attributes;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.tables.ColumnSort;

public interface AttributeService extends AuthenticatedService {

	public List<AttributeCategory> getCategories();

	public AttributeCategory getAttributeCategoryById(Long id);

	public Attribute getAttributeById(Long id);

	public AttributeCategory createAttributeCategory(String name,
			String context, int weight) throws ResourceCreationException;

	public Attribute updateAttribute(Attribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, Boolean readOnly, Boolean encrypted,
			String variableName) throws ResourceCreationException;

	public Attribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException;

	public void deleteAttribute(Attribute attribute);

	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting);

	public Long getAttributeCount(String searchPattern);
}
