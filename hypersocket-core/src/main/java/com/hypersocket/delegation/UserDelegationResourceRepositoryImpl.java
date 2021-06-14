package com.hypersocket.delegation;

import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractAssignableResourceRepositoryImpl;

@Repository
public class UserDelegationResourceRepositoryImpl extends
		AbstractAssignableResourceRepositoryImpl<UserDelegationResource> implements
		UserDelegationResourceRepository {

	/**
	 * TODO rename this class to match your entity / interface
	 */
	@Override
	protected Class<UserDelegationResource> getResourceClass() {
		return UserDelegationResource.class;
	}

}
