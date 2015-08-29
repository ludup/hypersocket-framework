package com.hypersocket.interfaceState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;

@Service
public class UserInterfaceStateServiceImpl extends
		AbstractAuthenticatedServiceImpl implements UserInterfaceStateService {

	public static final String RESOURCE_BUNDLE = "UserInterfaceStateService";

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
	public UserInterfaceState getSpecificStateByResourceId(Long resourceId) {
		return repository.getStateByResourceId(resourceId,
				getCurrentPrincipal().getId());
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			Long top, Long left, String name, boolean specific)
			throws AccessDeniedException {

		newState.setTop(top);
		newState.setLeftpx(left);
		newState.setName(name);
		if (specific) {
			newState.setPrincipalId(getCurrentPrincipal().getId());
		}
		repository.updateState(newState);
		return newState;
	}

	@Override
	public UserInterfaceState createState(Long resourceId, Long top, Long left,
			String name, boolean specific) throws AccessDeniedException {

		UserInterfaceState newState = new UserInterfaceState();

		newState.setResourceId(resourceId);
		newState.setTop(top);
		newState.setLeftpx(left);
		newState.setName(name);
		if (specific) {
			newState.setPrincipalId(getCurrentPrincipal().getId());
		}
		repository.updateState(newState);

		return this.getStateByResourceId(resourceId);

	}

	@Override
	public List<UserInterfaceState> getStates(Long[] resources, boolean specific)
			throws AccessDeniedException {

		List<UserInterfaceState> stateList = new ArrayList<UserInterfaceState>();
		for (Long resourceId : resources) {
			UserInterfaceState state;
			if (specific) {
				state = getSpecificStateByResourceId(resourceId);
			} else {
				state = getStateByResourceId(resourceId);
			}

			if (state != null) {
				stateList.add(state);
			}
		}
		return stateList;
	}
}
