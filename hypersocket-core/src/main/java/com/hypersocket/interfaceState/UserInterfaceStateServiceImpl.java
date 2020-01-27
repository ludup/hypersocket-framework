package com.hypersocket.interfaceState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;

@Service
public class UserInterfaceStateServiceImpl extends
		AbstractAuthenticatedServiceImpl implements UserInterfaceStateService {

	public static final String RESOURCE_BUNDLE = "UserInterfaceStateService";

	public static final String RESOURCE_CATEGORY_TABLE_STATE = "userInterfaceState";
	
	private List<UserInterfaceStateListener> listeners = new ArrayList<UserInterfaceStateListener>();

	@Autowired
	private UserInterfaceStateRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private RealmService realmService; 
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		realmService.registerRealmListener(new RealmAdapter() {
			@Override
			public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {
				repository.deleteRealm(realm);
			}	
		});
	}

	@Override
	public UserInterfaceState getStateByResourceId(Long resourceId) {
		return repository.getStateByResourceId(resourceId);
	}

	@Override
	public UserInterfaceState getStateByName(String name)
			throws AccessDeniedException {
		return repository.getResourceByName(name, getCurrentRealm());
	}
	
	@Override
	public UserInterfaceState getStateByName(String name, Realm realm){
		return repository.getResourceByName(name, realm);
	}
	
	@Override
	public Collection<UserInterfaceState> getStateStartsWith(String name, Realm realm){
		return repository.getStateStartsWith(name, RESOURCE_CATEGORY_TABLE_STATE, realm);
	}
	
	@Override
	public UserInterfaceState getStateByName(String name, boolean specific)
			throws AccessDeniedException {
		if(specific){
			return getStateByName(name + "_" + getCurrentPrincipal().getId());
		}else{
			return getStateByName(name);
		}
	}
	
	@Override
	public UserInterfaceState getStateByName(Principal principal, String name){
		Realm realm = principal.getRealm();
		return repository.getResourceByName(name + "_" + principal.getId(), realm);
	}

	@Override
	public UserInterfaceState updateState(UserInterfaceState newState,
			String preferences) throws AccessDeniedException {

		newState.setPreferences(preferences);
		repository.updateState(newState);
		for (UserInterfaceStateListener listener : listeners) {
			listener.modifyState(newState);
		}
		return getStateByResourceId(newState.getId());
	}

	@Override
	public UserInterfaceState createState(Principal principal,
			String preferences, String name) throws AccessDeniedException {

		UserInterfaceState newState = new UserInterfaceState();
		newState.setPreferences(preferences);
		if (principal != null) {
			newState.setName(name + "_" + principal.getId());
		} else {
			newState.setName(name);
		}
		newState.setResourceCategory(RESOURCE_CATEGORY_TABLE_STATE);
		newState.setRealm(getCurrentRealm());
		repository.updateState(newState);

		if (principal != null) {
			newState = repository.getResourceByName(
					name + "_" + principal.getId(), getCurrentRealm());
		} else {
			newState = getStateByName(name);
		}
		for (UserInterfaceStateListener listener : listeners) {
			listener.modifyState(newState);
		}
		return getStateByResourceId(newState.getId());

	}
	
	@Override
	public UserInterfaceState createState(Principal principal,
			String preferences, String name, Realm realm) throws AccessDeniedException {

		UserInterfaceState newState = new UserInterfaceState();
		newState.setPreferences(preferences);
		if (principal != null) {
			newState.setName(name + "_" + principal.getId());
		} else {
			newState.setName(name);
		}
		newState.setResourceCategory(RESOURCE_CATEGORY_TABLE_STATE);
		newState.setRealm(realm);
		repository.updateState(newState);

		if (principal != null) {
			newState = repository.getResourceByName(
					name + "_" + principal.getId(), realm);
		} else {
			newState = getStateByName(name);
		}
		for (UserInterfaceStateListener listener : listeners) {
			listener.modifyState(newState);
		}
		return getStateByResourceId(newState.getId());

	}

	@Override
	public Collection<UserInterfaceState> getStates(String[] resources,
			boolean specific) throws AccessDeniedException {
		Collection<UserInterfaceState> userInterfaceStateList = new ArrayList<UserInterfaceState>();
		for (String resourceName : resources) {
			UserInterfaceState state;
			if(specific){
				state = getStateByName(resourceName + "_" + getCurrentPrincipal().getId());
			}else{
				state = getStateByName(resourceName);
			}
			if (state != null) {
				userInterfaceStateList.add(state);
			}
		}
		return userInterfaceStateList;
	}
	
	@Override
	public void registerListener(UserInterfaceStateListener listener) {
		listeners.add(listener);
	}
}
