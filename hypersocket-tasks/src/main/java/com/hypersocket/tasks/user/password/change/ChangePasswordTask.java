package com.hypersocket.tasks.user.password.change;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.password.policy.PasswordPolicyResourceService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class ChangePasswordTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "changePasswordTask";

	public static final String RESOURCE_BUNDLE = "ChangePasswordTask";
	
	@Autowired
	private ChangePasswordTaskRepository repository;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService; 

	@Autowired
	private RealmService realmService;
	
	@Autowired
	private PasswordPolicyResourceService policyService; 
	
	public ChangePasswordTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(ChangePasswordTaskResult.class,
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

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException {

		String principalName = processTokenReplacements(repository.getValue(task, "changePassword.principalName"), event);
		boolean forceChange = repository.getBooleanValue(task, "changePassword.forceChange");
		boolean generate = repository.getBooleanValue(task, "changePassword.generate");
		try {
			
			Principal principal = realmService.getPrincipalByName(currentRealm, principalName, PrincipalType.USER);	
			String password;
			if(generate) {
				password = policyService.generatePassword(policyService.resolvePolicy(principal));			
			} else {
				password = processTokenReplacements(repository.getDecryptedValue(task, "changePassword.password"), event);
			}
			
			realmService.setPassword(principal, password, forceChange, true);

			return new ChangePasswordTaskResult(this, currentRealm, task, principal, password, generate);
		} catch (AccessDeniedException | ResourceException e) {
			return new ChangePasswordTaskResult(this, e, currentRealm, task);
		}
	}
	
	public String getResultResourceKey() {
		return ChangePasswordTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return false;
	}

}
