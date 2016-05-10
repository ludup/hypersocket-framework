package com.hypersocket.interfaceState;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public interface UserInterfaceStateService {

	UserInterfaceState getStateByResourceId(Long resourceId);

	UserInterfaceState getStateByName(String name) throws AccessDeniedException;
	
	public UserInterfaceState getStateByName(String name, Realm realm);
	
	UserInterfaceState getStateByName(String name, boolean specific) throws AccessDeniedException;

	UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException;

	UserInterfaceState createState(Principal principal, String preferences,
			String name) throws AccessDeniedException;

	Collection<UserInterfaceState> getStates(String[] resources, boolean specific)
			throws AccessDeniedException;
	
	void registerListener(UserInterfaceStateListener listener);
}
