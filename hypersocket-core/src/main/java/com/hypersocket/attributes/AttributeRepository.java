package com.hypersocket.attributes;

import java.util.List;

import com.hypersocket.repository.AbstractEntityRepository;
import com.hypersocket.tables.ColumnSort;

public interface AttributeRepository extends AbstractEntityRepository<Attribute,Long> {

	void saveCategory(AttributeCategory cat);

	void saveAttribute(Attribute attr);
	
	List<Attribute> searchAttributes(String searchPattern,
			int start, int length, ColumnSort[] sorting);

	Long getAttributeCount(String searchPattern);

	boolean nameExists(Attribute attribute);

}
