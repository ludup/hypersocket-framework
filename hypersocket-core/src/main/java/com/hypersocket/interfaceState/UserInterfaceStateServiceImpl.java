package com.hypersocket.interfaceState;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;

@Service
public class UserInterfaceStateServiceImpl extends
		AbstractAuthenticatedServiceImpl implements UserInterfaceStateService {

	public static final String RESOURCE_BUNDLE = "UserInterfaceStateService";
	
	public static final String RESOURCE_CATEGORY_TABLE_STATE = "tableState";

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
	public UserInterfaceState getState(String name)
			throws AccessDeniedException {
		return repository.getResourceByName(name, getCurrentRealm());
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException {

		newState.setPreferences(preferences);
		repository.updateState(newState);
		return getStateByResourceId(newState.getId());
	}

	@Override
	public UserInterfaceState createState(Long principalId, String preferences,
			String name) throws AccessDeniedException {

		UserInterfaceState newState = new UserInterfaceState();

		newState.setName(name + "_" + principalId);
		newState.setPreferences(preferences);
		newState.setPrincipalId(principalId);
		newState.setResourceCategory(RESOURCE_CATEGORY_TABLE_STATE);
		newState.setRealm(getCurrentRealm());
		repository.updateState(newState);
		newState = repository.getResourceByName(name + "_" + principalId, getCurrentRealm());
		return getStateByResourceId(newState.getId());

	}
}
