package com.hypersocket.template;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.tables.ColumnSort;

@Repository
@Transactional
public class TemplateRepositoryImpl extends
		AbstractResourceRepositoryImpl<Template> implements TemplateRepository {

	@Override
	protected Class<Template> getResourceClass() {
		return Template.class;
	}
	
	@Override
	public List<Template> search(Realm realm, String searchPattern, final String type, int start, int length, ColumnSort[] sorting) {
		return super.search(getResourceClass(), "name", searchPattern, start, length, sorting, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("type", type));
			}
		});
	}
	
	@Override
	public long getResourceCount(Realm realm, String searchPattern, final String type) {
		return getCount(getResourceClass(), "name", searchPattern, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("type", type));
			}
		});
	}

}
