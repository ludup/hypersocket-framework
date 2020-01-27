package com.hypersocket.tasks.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.TaskResultHolder;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractUpdateUserTask extends AbstractAccountTask {

	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private EventService eventService; 
	
	@Override
	protected TaskResult doExecute(Principal principal, Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException {
		
		
		List<Principal> assosciated = realmService.getAssociatedPrincipals(principal);
		
		for(String group : ResourceUtils.explodeCollectionValues(getRepository().getValue(task, "updateUser.removeGroups"))) {
			for(String groupName : processTokenReplacements(group, event).split(",")) {
				Principal g = realmService.getPrincipalByName(currentRealm, groupName, PrincipalType.GROUP);
				if(g!=null) {
					assosciated.remove(g);
				}
			}
		}
		
		for(String group : ResourceUtils.explodeCollectionValues(getRepository().getValue(task, "updateUser.addGroups"))) {
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
			realmService.updateUser(currentRealm, principal, principal.getName(), properties, assosciated);
			return new TaskResultHolder(eventService.getLastResult(), true);
		} catch (ResourceException | AccessDeniedException e) {
			return new TaskResultHolder(eventService.getLastResult(), true);
		} finally {
			eventService.undelayEvents();
		}
	}

}
