package com.hypersocket.attributes.user;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class UserAttributeCategoryRepositoryImpl extends
		AbstractResourceRepositoryImpl<UserAttributeCategory> implements
		UserAttributeCategoryRepository {

	@Override
	protected Class<UserAttributeCategory> getResourceClass() {
		return UserAttributeCategory.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<UserAttributeCategory> allResources() {
		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		return (List<UserAttributeCategory>) crit.list();
	}

	@Override
	@Transactional(readOnly=true)
	public Long getMaximumCategoryWeight() {
		return max("weight", getResourceClass(), new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("deleted", false));
			}
		});

	}
	

}
