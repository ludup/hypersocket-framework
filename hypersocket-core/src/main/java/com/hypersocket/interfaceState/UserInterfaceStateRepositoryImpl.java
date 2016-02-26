package com.hypersocket.interfaceState;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class UserInterfaceStateRepositoryImpl extends
		AbstractResourceRepositoryImpl<UserInterfaceState> implements
		UserInterfaceStateRepository {

	@Override
	@Transactional(readOnly=true)
	public UserInterfaceState getStateByResourceId(final Long resourceId) {
		return get("id", resourceId, UserInterfaceState.class);
	}

	@Override
	@Transactional
	public void updateState(UserInterfaceState newState) {
		save(newState);
	}

	@Override
	protected Class<UserInterfaceState> getResourceClass() {
		return UserInterfaceState.class;
	}

}
