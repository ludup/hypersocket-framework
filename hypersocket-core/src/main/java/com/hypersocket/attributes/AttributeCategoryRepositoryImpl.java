package com.hypersocket.attributes;

import java.util.List;
import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DetachedCriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

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
	public boolean nameExists(final AttributeCategory newCategory) {
		AttributeCategory attributeCategory = get(getEntityClass(),
				new DetachedCriteriaConfiguration() {
					@Override
					public void configure(DetachedCriteria criteria) {
						criteria.add(Restrictions.eq("name",
								newCategory.getName()));
						if (newCategory.getId() != null) {
							criteria.add(Restrictions.ne("id",
									newCategory.getId()));
						}
					}
				});

		return attributeCategory != null;
	}

	@Override
	public List<AttributeCategory> searchAttributeCategories(
			String searchPattern, int start, int length, ColumnSort[] sorting) {
		return search(getEntityClass(), "name", searchPattern, start, length,
				sorting, new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}

	@Override
	public Long getAttributeCategoryCount(String searchPattern) {
		return getCount(getEntityClass(), "name", searchPattern,
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}
}
