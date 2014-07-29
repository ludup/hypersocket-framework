package com.hypersocket.attributes;

import com.hypersocket.repository.AbstractEntityRepository;

public interface AttributeRepository extends AbstractEntityRepository<Attribute,Long> {

	void saveCategory(AttributeCategory cat);

	void saveAttribute(Attribute attr);

}
