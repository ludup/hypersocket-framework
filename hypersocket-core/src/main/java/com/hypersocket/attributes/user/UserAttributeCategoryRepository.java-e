package com.hypersocket.attributes;

import java.util.List;

import com.hypersocket.repository.AbstractEntityRepository;
import com.hypersocket.tables.ColumnSort;

public interface AttributeCategoryRepository extends
		AbstractEntityRepository<AttributeCategory, Long> {

	boolean nameExists(AttributeCategory category);

	void saveCategory(AttributeCategory cat);
	
	List<AttributeCategory> searchAttributeCategories(String searchPattern,
			int start, int length, ColumnSort[] sorting);

	Long getAttributeCategoryCount(String searchPattern);
	
	AttributeCategory getCategoryByName(String name);

	Long getMaximumCategoryWeight();

}
