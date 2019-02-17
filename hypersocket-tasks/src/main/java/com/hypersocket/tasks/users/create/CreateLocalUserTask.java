package com.hypersocket.tasks.users.create;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.password.policy.PasswordPolicyPasswordCreator;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.DefaultPasswordCreator;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.user.AbstractCreateUserTask;

@Component
public class CreateLocalUserTask extends AbstractCreateUserTask {

	public static final String TASK_RESOURCE_KEY = "createLocalUserTask";

	public static final String RESOURCE_BUNDLE = "CreateLocalUserTask";
	
	@Autowired
	CreateLocalUserTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	@Autowired
	RealmService realmService; 
	
	public CreateLocalUserTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(CreateLocalUserTaskResult.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { TASK_RESOURCE_KEY };
	}

	public String getResultResourceKey() {
		return CreateLocalUserTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return false;
	}
	
	protected void doCreateUser(Realm currentRealm, String principalName, Map<String,String> properties, 
			List<Principal> associated, boolean generatePassword, String staticPassword, boolean forceChange, boolean sendNotifications) throws AccessDeniedException, ResourceException {
		realmService.createLocalUser(currentRealm, principalName, properties, associated, 
				generatePassword ? new PasswordPolicyPasswordCreator() : new DefaultPasswordCreator(staticPassword), 
						forceChange, false, sendNotifications);
	}

}
