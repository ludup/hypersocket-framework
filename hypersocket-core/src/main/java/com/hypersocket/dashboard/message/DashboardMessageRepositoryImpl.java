package com.hypersocket.dashboard.message;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class DashboardMessageRepositoryImpl extends
		AbstractResourceRepositoryImpl<DashboardMessage> implements
		DashboardMessageRepository {

	@Override
	public void saveNewMessages(List<DashboardMessage> dashboardMessageList) {
		for (DashboardMessage message : dashboardMessageList) {
			save(message);
		}
	}

	@Override
	public List<DashboardMessage> getMessages() {
		return allEntities(getResourceClass(), new DeletedCriteria(
				false), new DistinctRootEntity(),
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.addOrder(Order.asc("created"));
					}
				});
	}

	@Override
	public DashboardMessage getMessage(DashboardMessage dashboardMessage) {
		return get("messageId", dashboardMessage.getMessageId(),
				getResourceClass());
	}

	@Override
	public void saveNewMessage(DashboardMessage dashboardMessage) {
		save(dashboardMessage);
	}

	@Override
	public List<DashboardMessage> getUnexpiredMessages(final int pageNum) {
		return allEntities(getResourceClass(), new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.addOrder(Order.desc("created"))
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.gt("expires", new Date()))
						.setFirstResult(pageNum * 3).setMaxResults(3);
			}
		});
	}

	@Override
	protected Class<DashboardMessage> getResourceClass() {
		return DashboardMessage.class;
	}

	@Override
	public Long getMessageCount() {
		return getCount(getResourceClass(), "name", "",
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("deleted", false)).add(
								Restrictions.gt("expires", new Date()));
					}
				});
	}

}
