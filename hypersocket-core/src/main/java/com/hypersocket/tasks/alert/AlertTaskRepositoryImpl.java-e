package com.hypersocket.tasks.alert;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tasks.Task;

@Repository
public class AlertTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements AlertTaskRepository {

	public AlertTaskRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/generateAlert.xml");
	}

	@Override
	@Transactional(readOnly=true)
	public long getKeyCount(final Task task, final String key,
			final Date since) {

		return getCount(AlertKey.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("key", key));
				criteria.add(Restrictions.eq("task", task));
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
	public void deleteKeys(Task task, String key) {
		String hql = "delete from AlertKey a where a.task = :task and a.key = :key";
		createQuery(hql, true)
				.setParameter("task", task).setParameter("key", key)
				.executeUpdate();
	}
}
