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
	private RealmService realmService; 
	
	@Autowired
	private EventService eventService; 
	
	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event) throws ValidationException {

		String principalName = processTokenReplacements(getRepository().getValue(task, "createUser.principalName"), event);
		boolean sendNotifications = getRepository().getBooleanValue(task, "createUser.sendNotification");
		boolean forceChange = getRepository().getBooleanValue(task, "createUser.forceChange");
		boolean generatePassword = getRepository().getBooleanValue(task, "createUser.generatePassword");
		String staticPassword = processTokenReplacements(getRepository().getValue(task, "createUser.defaultPassword"), event);
		
		List<Principal> assosciated = new ArrayList<Principal>(); 

		for(String group : ResourceUtils.explodeCollectionValues(getRepository().getValue(task, "createUser.groups"))) {
			for(String groupName : processTokenReplacements(group, event).split(",")) {
				Principal g = realmService.getPrincipalByName(currentRealm, groupName, PrincipalType.GROUP);
				if(g!=null) {
					assosciated.add(g);
				}
			}
		}
		
		Map<String,String> properties = new HashMap<String,String>();
		for(PropertyTemplate t : getRepository().getPropertyTemplates(null)) {
			String value = processTokenReplacements(getRepository().getValue(task, t.getResourceKey()), event);
			if(StringUtils.isNotBlank(value)) {
				properties.put(t.getResourceKey(), value);
			}
		}
		
		eventService.delayEvents();
		
		try {	
			
			doCreateUser(currentRealm, principalName, properties, assosciated, generatePassword, staticPassword, 
					forceChange, sendNotifications);
			
			return new TaskResultHolder(eventService.getLastResult(), true);
				
		} catch (ResourceException | AccessDeniedException e) {
			return new TaskResultHolder(eventService.getLastResult(), true);
		} finally {
			eventService.undelayEvents();
			eventService.rollbackDelayedEvents(false);
		}

		
	}
	
	protected void doCreateUser(Realm currentRealm, String principalName, Map<String,String> properties, 
			List<Principal> associated, boolean generatePassword, String staticPassword, boolean forceChange, boolean sendNotifications) throws AccessDeniedException, ResourceException {
		realmService.createUser(currentRealm, principalName, properties, associated, 
				generatePassword ? new PasswordPolicyPasswordCreator() : new DefaultPasswordCreator(staticPassword), 
						forceChange, false, sendNotifications);
	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
