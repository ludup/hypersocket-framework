package com.hypersocket.interfaceState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.interfaceState.event.UserInterfaceStateCreatedEvent;
import com.hypersocket.interfaceState.event.UserInterfaceStateDeletedEvent;
import com.hypersocket.interfaceState.event.UserInterfaceStateEvent;
import com.hypersocket.interfaceState.event.UserInterfaceStateUpdatedEvent;
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

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.userInterfaceStates");

		for (UserInterfaceStatePermission p : UserInterfaceStatePermission
				.values()) {
			permissionService.registerPermission(p, cat);
		}

		eventService.registerEvent(UserInterfaceStateEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserInterfaceStateCreatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserInterfaceStateUpdatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserInterfaceStateDeletedEvent.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public UserInterfaceState getStateByResourceId(Long resourceId) {
		return repository.getStateByResourceId(resourceId);
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			Long top, Long left, String name) throws AccessDeniedException {
		assertPermission(UserInterfaceStatePermission.UPDATE);
		try {
			newState.setTop(top);
			newState.setLeftpx(left);
			newState.setName(name);
			repository.updateState(newState);
			
			//TODO change to update event
			eventService.publishEvent(new UserInterfaceStateCreatedEvent(this,
					getCurrentSession(), newState));
			return newState;
		} catch (Exception e) {
			eventService.publishEvent(new UserInterfaceStateUpdatedEvent(this,
					e, getCurrentSession(), newState));
			throw e;
		}
	}

	@Override
	public UserInterfaceState createState(Long resourceId, Long top, Long left,
			String name) throws AccessDeniedException {
		assertPermission(UserInterfaceStatePermission.CREATE);
		UserInterfaceState newState = new UserInterfaceState();
		try {
			newState.setResourceId(resourceId);
			newState.setTop(top);
			newState.setLeftpx(left);
			newState.setName(name);
			repository.updateState(newState);
			eventService.publishEvent(new UserInterfaceStateCreatedEvent(this,
					getCurrentSession(), newState));
			return this.getStateByResourceId(resourceId);
		} catch (Exception e) {
			eventService.publishEvent(new UserInterfaceStateCreatedEvent(this,
					e, getCurrentSession(), newState));
			throw e;
		}

	}

	@Override
	public List<UserInterfaceState> getStates(Long[] resources)
			throws AccessDeniedException {
		assertPermission(UserInterfaceStatePermission.READ);
		List<UserInterfaceState> stateList = new ArrayList<UserInterfaceState>();
		for (Long resourceId : resources) {
			UserInterfaceState state = getStateByResourceId(resourceId);
			if (state != null) {
				stateList.add(state);
			}
		}
		return stateList;
	}
}
