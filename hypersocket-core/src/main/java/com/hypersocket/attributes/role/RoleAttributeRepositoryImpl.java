package com.hypersocket.attributes.role;

import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.resource.AbstractAssignableResourceRepositoryImpl;

@Repository
public class RoleAttributeRepositoryImpl extends
		AbstractAssignableResourceRepositoryImpl<RoleAttribute> implements RoleAttributeRepository {


	protected void afterSave(RoleAttribute attr, java.util.Map<String,String> properties) {
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("RoleAttributes",
				attr.getVariableName(), "", attr.getName()));

		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message("RoleAttributes", attr.getVariableName() + ".info", "", attr
						.getDescription()));

		I18N.flushOverrides();
	}
	
	@Override
	protected Class<RoleAttribute> getResourceClass() {
		return RoleAttribute.class;
	}


	@Override
	@Transactional(readOnly=true)
	public Long getMaximumAttributeWeight(final RoleAttributeCategory cat) {
		return max("weight", getResourceClass(), new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("category", cat));
				criteria.add(Restrictions.eq("deleted", false));
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public RoleAttribute getAttributeByVariableName(String attributeName, Realm realm) {
		return get("variableName", 
				attributeName, 
				getResourceClass(), 
				new RealmRestriction(realm), 
				new DeletedCriteria(false));
	}
	
	

}
