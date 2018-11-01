package com.hypersocket.tasks.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.password.policy.PasswordPolicyPasswordCreator;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.DefaultPasswordCreator;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.TaskResultHolder;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractCreateUserTask extends AbstractTaskProvider {

	@Autowired
	RealmService realmService; 
	
	@Autowired
	EventService eventService; 
	
	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event) throws ValidationException {

		String principalName = processTokenReplacements(getRepository().getValue(task, "createUser.principalName"), event);
		boolean sendNotifications = Boolean.valueOf(processTokenReplacements(getRepository().getValue(task, "createUser.sendNotification"), event));
		boolean forceChange = Boolean.valueOf(processTokenReplacements(getRepository().getValue(task, "createUser.forceChange"), event));
		boolean generatePassword = Boolean.valueOf(processTokenReplacements(getRepository().getValue(task, "createUser.generatePassword"), event));
		String staticPassword = processTokenReplacements(getRepository().getValue(task, "createUser.defaultPassword"), event);
		
		List<Principal> assosciated = new ArrayList<Principal>(); 
		for(String group : ResourceUtils.explodeCollectionValues(processTokenReplacements(getRepository().getValue(task, "createUser.groups"), event))) {
			Principal g = realmService.getPrincipalByName(currentRealm, group, PrincipalType.GROUP);
			if(g!=null) {
				assosciated.add(g);
			}
		}
		
		Map<String,String> properties = new HashMap<String,String>();
		for(PropertyTemplate t : getRepository().getPropertyTemplates(null)) {
			String value = processTokenReplacements(getRepository().getValue(task, t.getResourceKey()), event);
			if(StringUtils.isNotBlank(value)) {
				properties.put(t.getResourceKey(), value);
			}
		}
		
		eventService.delayEvents(true);
		
		try {	
			
			realmService.createUser(currentRealm, principalName, properties, assosciated, 
					generatePassword ? new PasswordPolicyPasswordCreator() : new DefaultPasswordCreator(staticPassword), 
							forceChange, false, sendNotifications);
			
			return new TaskResultHolder(eventService.getLastResult(), true);
				
		} catch (ResourceException | AccessDeniedException e) {
			return new TaskResultHolder(eventService.getLastResult(), true);
		} finally {
			eventService.delayEvents(false);
		}

	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
