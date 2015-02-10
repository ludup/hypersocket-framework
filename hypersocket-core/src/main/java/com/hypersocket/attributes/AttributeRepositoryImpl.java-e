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
public class AttributeRepositoryImpl extends
		AbstractEntityRepositoryImpl<Attribute, Long> implements
		AttributeRepository {

	@Override
	public void saveAttribute(Attribute attr) {
		saveEntity(attr);

		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes",
				"attribute" + String.valueOf(attr.getId()), "", attr.getName()));

		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message("UserAttributes", "attribute"
						+ String.valueOf(attr.getId()) + ".info", "", attr
						.getDescription()));

		I18N.flushOverrides();
	}

	@Override
	protected Class<Attribute> getEntityClass() {
		return Attribute.class;
	}

	@Override
	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		return search(getEntityClass(), "name", searchPattern, start, length,
				sorting, new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}

	@Override
	public Long getAttributeCount(String searchPattern) {
		return getCount(getEntityClass(), "name", searchPattern,
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("deleted", false));
					}
				});
	}

	@Override
	public boolean nameExists(final Attribute newAttribute) {
		Attribute attribute = get(getEntityClass(),
				new DetachedCriteriaConfiguration() {
					@Override
					public void configure(DetachedCriteria criteria) {
						criteria.add(Restrictions.eq("name",
								newAttribute.getName()));
						if (newAttribute.getId() != null) {
							criteria.add(Restrictions.ne("id",
									newAttribute.getId()));
						}
					}
				});

		return attribute != null;
	}

}
