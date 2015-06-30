package com.hypersocket.interfaceState;

import com.hypersocket.resource.AbstractResourceRepository;

public interface UserInterfaceStateRepository extends
		AbstractResourceRepository<UserInterfaceState> {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState getStateByResourceId(Long resourceId, Long principalId);

	void updateState(UserInterfaceState newState);

}
