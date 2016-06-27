package com.hypersocket.attributes.role;

import java.util.List;
import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class RoleAttributeCategoryRepositoryImpl extends
		AbstractResourceRepositoryImpl<RoleAttributeCategory> implements
		RoleAttributeCategoryRepository {

	@Override
	protected Class<RoleAttributeCategory> getResourceClass() {
		return RoleAttributeCategory.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<RoleAttributeCategory> allResources() {
		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		return (List<RoleAttributeCategory>) crit.list();
	}

	protected void afterSave(RoleAttributeCategory resource, java.util.Map<String,String> properties) {
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("RoleAttributes",
				"attributeCategory" + String.valueOf(resource.getId()) + ".label",
				"", resource.getName()));
		I18N.flushOverrides();
	};
	
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
