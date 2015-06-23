package com.hypersocket.interfaceState;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.DetachedCriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class UserInterfaceStateRepositoryImpl extends
		AbstractResourceRepositoryImpl<UserInterfaceState> implements
		UserInterfaceStateRepository {

	@Override
	public UserInterfaceState getStateByResourceId(final Long resourceId) {
		return get("resourceId", resourceId, UserInterfaceState.class,
				new DetachedCriteriaConfiguration() {
					@Override
					public void configure(DetachedCriteria criteria) {
						criteria.add(Restrictions.eq("resourceId", resourceId));
					}
				});
	}

	@Override
	public void updateState(UserInterfaceState newState) {
		save(newState);
	}

	@Override
	protected Class<UserInterfaceState> getResourceClass() {
		return UserInterfaceState.class;
	}

}
