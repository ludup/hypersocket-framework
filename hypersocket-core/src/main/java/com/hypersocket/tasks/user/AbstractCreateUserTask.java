package com.hypersocket.tasks.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.password.policy.PasswordPolicyPasswordCreator;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.DefaultPasswordCreator;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractCreateUserTask extends AbstractTaskProvider {

	@Autowired
	RealmService realmService; 
	
	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event) throws ValidationException {

		String principalName = getRepository().getValue(task, "createUser.principalName");
		boolean sendNotifications = getRepository().getBooleanValue(task, "createUser.sendNotification");
		boolean forceChange = getRepository().getBooleanValue(task, "createUser.forceChange");
		boolean generatePassword = getRepository().getBooleanValue(task, "createUser.generatePassword");
		String staticPassword = getRepository().getValue(task, "createUser.defaultPassword");
		
		List<Principal> assosciated = new ArrayList<Principal>(); 
		for(String group : getRepository().getValues(task, "createUser.groups")) {
			Principal g = realmService.getPrincipalByName(currentRealm, group, PrincipalType.GROUP);
			if(g!=null) {
				assosciated.add(g);
			}
		}
		
		Map<String,String> properties = new HashMap<String,String>();
		for(PropertyTemplate t : getRepository().getPropertyTemplates(null)) {
			String value = getRepository().getValue(task, t.getResourceKey());
			if(StringUtils.isNotBlank(value)) {
				properties.put(t.getResourceKey(), value);
			}
		}
		
		try {
			realmService.createUser(currentRealm, principalName, properties, assosciated, 
					generatePassword ? new PasswordPolicyPasswordCreator() : new DefaultPasswordCreator(staticPassword), 
							forceChange, false, sendNotifications);
			return null;
		} catch (ResourceException | AccessDeniedException e) {
			return null;
		}

	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
