package com.hypersocket.interfaceState;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;

@Service
public class UserInterfaceStateServiceImpl extends
		AbstractAuthenticatedServiceImpl implements UserInterfaceStateService {

	public static final String RESOURCE_BUNDLE = "UserInterfaceStateService";

	public static final String RESOURCE_CATEGORY_TABLE_STATE = "userInterfaceState";

	@Autowired
	UserInterfaceStateRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	@Override
	public UserInterfaceState getStateByResourceId(Long resourceId) {
		return repository.getStateByResourceId(resourceId);
	}

	@Override
	public UserInterfaceState getState(String name, Principal principal)
			throws AccessDeniedException {
		return repository.getState(name, principal.getId(),
				RESOURCE_CATEGORY_TABLE_STATE, getCurrentRealm());
	}

	@Override
	public UserInterfaceState getState(String name)
			throws AccessDeniedException {
		return repository.getState(name, RESOURCE_CATEGORY_TABLE_STATE,
				getCurrentRealm());
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException {

		newState.setPreferences(preferences);
		repository.updateState(newState);
		return getStateByResourceId(newState.getId());
	}

	@Override
	public UserInterfaceState createState(Principal principal,
			Long bindResourceId, String preferences, String name)
			throws AccessDeniedException {

		UserInterfaceState newState = new UserInterfaceState();

		newState.setName(name);
		newState.setPreferences(preferences);
		if (principal != null) {
			newState.setPrincipalId(principal.getId());
		}
		if (bindResourceId != null) {
			newState.setBindResourceId(bindResourceId);
		}
		newState.setResourceCategory(RESOURCE_CATEGORY_TABLE_STATE);
		newState.setRealm(getCurrentRealm());
		repository.updateState(newState);

		if (principal != null) {
			newState = getState(name, principal);
		} else {
			newState = getState(name);
		}
		return getStateByResourceId(newState.getId());

	}

	@Override
	public Collection<UserInterfaceState> getStates(Long[] resources,
			boolean specific) throws AccessDeniedException {
		Collection<UserInterfaceState> userInterfaceStateList = new ArrayList<UserInterfaceState>();
		for (Long bindResourceId : resources) {
			UserInterfaceState state;
			if (specific) {
				state = repository.getStateByBindResourceId(bindResourceId,
						getCurrentPrincipal().getId());
			} else {
				state = repository.getStateByBindResourceId(bindResourceId);
			}

			if (state != null) {
				userInterfaceStateList.add(state);
			}
		}
		return userInterfaceStateList;
	}
}
