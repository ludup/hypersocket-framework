package com.hypersocket.attributes;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;

@Repository
@Transactional
public class AttributeRepositoryImpl extends
		AbstractRepositoryImpl<Long> implements AttributeRepository {


	public List<AttributeCategory> getCategories(String attributeContext) {
		return null;
	}


}
