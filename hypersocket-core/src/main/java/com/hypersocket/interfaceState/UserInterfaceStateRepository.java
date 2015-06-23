package com.hypersocket.interfaceState;

import com.hypersocket.resource.AbstractResourceRepository;

public interface UserInterfaceStateRepository extends
		AbstractResourceRepository<UserInterfaceState> {

	UserInterfaceState getStateByResourceId(Long resourceId);

	void updateState(UserInterfaceState newState);

}
