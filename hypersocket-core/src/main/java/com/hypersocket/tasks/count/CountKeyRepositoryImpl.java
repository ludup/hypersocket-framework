package com.hypersocket.tasks.count;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class CountKeyRepositoryImpl extends AbstractResourceRepositoryImpl<CountKey> implements CountKeyRepository  {

	@Override
	@Transactional(readOnly=true)
	public CountKey getCountKey(Realm realm, String resourceKey) {
		
		CountKey key = get("name", resourceKey, CountKey.class, new RealmRestriction(realm));
		if(Objects.isNull(key)) {
			key = new CountKey();
			key.setName(resourceKey);
			key.setRealm(realm);
			key.setCount(0L);
		}
		return key;
	}

	@Override
	protected Class<CountKey> getResourceClass() {
		return CountKey.class;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<CountKey> getCountValues(Realm realm, String[] resourceKeys) {
		return list(new RealmRestriction(realm), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.in("name", Arrays.asList(resourceKeys)));
			}
			
		});
	}
}
