package com.hypersocket.attributes.user;

import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.Query;
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
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;


@Repository
public class UserAttributeRepositoryImpl extends
		AbstractAssignableResourceRepositoryImpl<UserAttribute> implements UserAttributeRepository {


	protected void afterSave(UserAttribute attr, java.util.Map<String,String> properties) {
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes",
				attr.getVariableName(), "", attr.getName()));

		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message("UserAttributes", attr.getVariableName() + ".info", "", attr
						.getDescription()));

	}
	
	@Override
	protected Class<UserAttribute> getResourceClass() {
		return UserAttribute.class;
	}


	@Override
	@Transactional(readOnly=true)
	public Long getMaximumAttributeWeight(final UserAttributeCategory cat) {
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
	public UserAttribute getAttributeByVariableName(String attributeName, Realm realm) {
		return get("variableName", 
				attributeName, 
				getResourceClass(), 
				new RealmRestriction(realm), 
				new DeletedCriteria(false));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void deleteResource(UserAttribute resource, TransactionOperation<UserAttribute>... ops) throws ResourceException {
	
		Query query = createQuery("delete from DatabaseProperty where resourceKey = :resourceKey", true);
		query.setParameter("resourceKey", resource.getVariableName());
		query.executeUpdate();
		
		super.deleteResource(resource);
	}

}
