package com.hypersocket.interfaceState;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;

public interface UserInterfaceStateService {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState getState(String name) throws AccessDeniedException;

	UserInterfaceState getState(String name, Principal principal)
			throws AccessDeniedException;

	UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException;

	UserInterfaceState createState(Principal principal, Long bindResourceId,
			String preferences, String name) throws AccessDeniedException;

	Collection<UserInterfaceState> getStates(Long[] resources, boolean specific)
			throws AccessDeniedException;
}
