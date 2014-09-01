package com.hypersocket.triggers.actions.alert;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.triggers.TriggerAction;

@Repository
@Transactional
public class AlertTriggerActionRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements AlertTriggerActionRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("actions/alert-template.xml");
	}

	@Override
	public long getKeyCount(final TriggerAction action, final String key,
			final Date since) {

		return getCount(AlertKey.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("key", key));
				criteria.add(Restrictions.eq("action", action));
				criteria.add(Restrictions.gt("triggered", since));
			}
		});
	}

	@Override
	public void saveKey(AlertKey ak) {
		hibernateTemplate.save(ak);
	}

	@Override
	public void deleteKeys(TriggerAction action, String key) {
		String hql = "delete from AlertKey a where a.action = :action and a.key = :key";
		sessionFactory.getCurrentSession().createQuery(hql)
				.setParameter("action", action).setParameter("key", key)
				.executeUpdate();
	}
}
