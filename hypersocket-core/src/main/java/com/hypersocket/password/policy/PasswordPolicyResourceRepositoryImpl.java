package com.hypersocket.password.policy;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractAssignableResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;

@Repository
public class PasswordPolicyResourceRepositoryImpl extends
		AbstractAssignableResourceRepositoryImpl<PasswordPolicyResource> implements
		PasswordPolicyResourceRepository {

	@Override
	protected Class<PasswordPolicyResource> getResourceClass() {
		return PasswordPolicyResource.class;
	}

	@Override
	@Transactional(readOnly=true)
	public PasswordPolicyResource getPolicyByDN(String dn, Realm realm) {
		return get("dn", dn, PasswordPolicyResource.class, new RealmCriteria(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public PasswordPolicyResource getDefaultPolicyByModule(Realm realm, String moduleName) {
		return get("provider", moduleName, PasswordPolicyResource.class, new RealmCriteria(realm), 
				new CriteriaConfiguration() {
				@Override
				public void configure(Criteria criteria) {
					criteria.add(Restrictions.eq("defaultPolicy", true));
				}
		});
	}


}
