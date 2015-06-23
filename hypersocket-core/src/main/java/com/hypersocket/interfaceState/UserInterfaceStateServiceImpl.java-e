package com.hypersocket.interfaceState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;

@Service
public class UserInterfaceStateServiceImpl extends
		AbstractAuthenticatedServiceImpl implements UserInterfaceStateService {
	
	public static final String RESOURCE_BUNDLE = "UserInterfaceStateService";

	@Autowired
	UserInterfaceStateRepository repository;

	@PostConstruct
	private void postConstruct() {

	}

	@Override
	public UserInterfaceState getStateByResourceId(Long resourceId) {
		return repository.getStateByResourceId(resourceId);
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			Long top, Long left) {
		newState.setTop(top);
		newState.setLeftpx(left);
		repository.updateState(newState);
		return newState;
	}

	@Override
	public UserInterfaceState createState(Long resourceId, Long top, Long left) {
		UserInterfaceState newState = new UserInterfaceState();
		newState.setResourceId(resourceId);
		newState.setTop(top);
		newState.setLeftpx(left);
		repository.createState(newState);
		return this.getStateByResourceId(resourceId);
	}

	@Override
	public List<UserInterfaceState> getStates(Long[] resources) {
		List<UserInterfaceState> stateList = new ArrayList<UserInterfaceState>();
		for(Long resourceId: resources){
			UserInterfaceState state = getStateByResourceId(resourceId);
			if(state != null){
				stateList.add(state);
			}
		}
		return stateList;
	}
}
