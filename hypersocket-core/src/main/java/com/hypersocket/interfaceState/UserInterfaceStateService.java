package com.hypersocket.interfaceState;

import java.util.List;

import com.hypersocket.permissions.AccessDeniedException;

public interface UserInterfaceStateService {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState updateState(UserInterfaceState newState, Long top,
			Long left, String name) throws AccessDeniedException;

	UserInterfaceState createState(Long resourceId, Long top, Long left,
			String name) throws AccessDeniedException;

	List<UserInterfaceState> getStates(Long[] resources)
			throws AccessDeniedException;

}
