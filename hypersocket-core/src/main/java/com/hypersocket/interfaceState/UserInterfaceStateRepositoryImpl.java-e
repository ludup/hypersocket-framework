package com.hypersocket.interfaceState;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class UserInterfaceStateRepositoryImpl extends
		AbstractResourceRepositoryImpl<UserInterfaceState> implements
		UserInterfaceStateRepository {

	@Override
	@Transactional(readOnly = true)
	public UserInterfaceState getStateByResourceId(final Long resourceId) {
		return get("id", resourceId, UserInterfaceState.class);
	}

	@Override
	@Transactional(readOnly = true)
	public UserInterfaceState getState(final String name,
			final Long principalId, final String resourceCategory,
			final Realm realm) {
		UserInterfaceState userInterface = get(getResourceClass(),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("name", name))
								.add(Restrictions
										.eq("principalId", principalId))
								.add(Restrictions.eq("resourceCategory",
										resourceCategory))
								.add(Restrictions.eq("realm", realm));
					}
				});
		return userInterface;
	}

	@Override
	@Transactional(readOnly = true)
	public UserInterfaceState getState(final String name,
			final String resourceCategory, final Realm realm) {
		UserInterfaceState userInterface = get(getResourceClass(),
				new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("name", name))
								.add(Restrictions.isNull("principalId"))
								.add(Restrictions.eq("resourceCategory",
										resourceCategory))
								.add(Restrictions.eq("realm", realm));
					}
				});
		return userInterface;
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
