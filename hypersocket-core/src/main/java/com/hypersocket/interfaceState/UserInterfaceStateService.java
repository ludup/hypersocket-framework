package com.hypersocket.interfaceState;

import java.util.List;

public interface UserInterfaceStateService {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState updateState(UserInterfaceState newState, Long top,
			Long left);

	UserInterfaceState createState(Long resourceId, Long top, Long left);

	List<UserInterfaceState> getStates(Long[] resources);

}
