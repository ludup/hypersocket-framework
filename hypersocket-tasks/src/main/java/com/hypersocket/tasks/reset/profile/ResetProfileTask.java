package com.hypersocket.tasks.reset.profile;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileCredentialsService;
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
public class ResetProfileTask extends AbstractTaskProvider {
	
	private static Logger log = LoggerFactory.getLogger(ResetProfileTask.class);
	
	public static final String TASK_RESOURCE_KEY = "resetProfileTask";

	public static final String RESOURCE_BUNDLE = "ResetProfileTask";
	
	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService; 

	@Autowired
	private RealmService realmService;
	
	@Autowired
	private ProfileCredentialsService profileCredentialsService;
	
	@Autowired
	private ResetProfileTaskRepository resetProfileTaskRepository;
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(ResetProfileTaskResult.class,
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
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		if (!parameters.containsKey("resetProfile.principalName")) {
			throw new ValidationException("Principal name is required");
		}
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event) throws ValidationException {
		String name = processTokenReplacements(resetProfileTaskRepository.getValue(task, "resetProfile.principalName"), event);
		
		log.info("Reseting profile for user {}.", name);
		
		try {
			Principal principal = realmService.getUniquePrincipal(name, PrincipalType.USER);
			profileCredentialsService.resetProfile(principal);
			return new ResetProfileTaskResult(this, currentRealm, task, principal);
		} catch (ResourceException | AccessDeniedException e) {
			log.error("Failed to reset profile for {}.", name, e);
			return new ResetProfileTaskResult(this, e, currentRealm, task, name);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return this.resetProfileTaskRepository;
	}

	@Override
	public String getResultResourceKey() {
		return ResetProfileTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
