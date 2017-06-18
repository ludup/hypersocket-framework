package com.hypersocket.alert;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class AlertKeyRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements AlertKeyRepository {

	public AlertKeyRepositoryImpl() {
		super(true);
	}

	@Override
	@Transactional(readOnly=true)
	public long getKeyCount(final String resourceKey, final String key,
			final Date since) {

		return getCount(AlertKey.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("key", key));
				criteria.add(Restrictions.eq("resourceKey", resourceKey));
				criteria.add(Restrictions.gt("triggered", since));
			}
		});
	}

	@Override
	@Transactional
	public void saveKey(AlertKey ak) {
		save(ak, ak.getId()==null);
	}

	@Override
	@Transactional
	public void deleteKeys(String resourceKey, String key) {
		String hql = "delete from AlertKey a where a.resourceKey = :resourceKey and a.key = :key";
		createQuery(hql, true)
				.setParameter("resourceKey", resourceKey).setParameter("key", key)
				.executeUpdate();
	}
}
