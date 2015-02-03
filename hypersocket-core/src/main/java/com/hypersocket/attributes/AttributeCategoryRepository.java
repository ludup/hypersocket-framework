package com.hypersocket.attributes;

import com.hypersocket.repository.AbstractEntityRepository;

public interface AttributeCategoryRepository extends
		AbstractEntityRepository<AttributeCategory, Long> {

	boolean nameExists(String category);

}
