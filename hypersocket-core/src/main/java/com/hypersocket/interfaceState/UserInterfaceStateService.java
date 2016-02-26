package com.hypersocket.interfaceState;

import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;

public interface UserInterfaceStateService {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState getState(String name) throws AccessDeniedException;

	UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException;

	UserInterfaceState createState(Long principalId, String preferences,
			String name) throws AccessDeniedException;
}
