package com.hypersocket.attributes;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.DetachedCriteriaConfiguration;

@Repository
@Transactional
public class AttributeCategoryRepositoryImpl extends
		AbstractEntityRepositoryImpl<AttributeCategory, Long> implements
		AttributeCategoryRepository {

	@Override
	protected Class<AttributeCategory> getEntityClass() {
		return AttributeCategory.class;
	}

	@Override
	public boolean nameExists(final String category) {
		AttributeCategory attributeCategory = get(getEntityClass(),
				new DetachedCriteriaConfiguration() {

					@Override
					public void configure(DetachedCriteria criteria) {
						criteria.add(Restrictions.eq("name", category));

					}
				});

		return attributeCategory != null;
	}
}
