package com.hypersocket.attributes;

import java.util.Locale;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
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
	public void saveCategory(AttributeCategory cat) {
		save(cat);

		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes",
				"attributeCategory" + String.valueOf(cat.getId()) + ".label",
				"", cat.getName()));
		I18N.flushOverrides();

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
