package com.hypersocket.tasks.suspend;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class ResumeUserTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(ResumeUserTask.class);

	public static final String RESOURCE_BUNDLE = "ResumeUserTask";

	public static final String RESOURCE_KEY = "resumeUser";

	@Autowired
	SuspendUserTask suspendUserTask;

	@Autowired
	PrincipalSuspensionService suspensionService;
	
	@Autowired
	ResumeUserTaskRepository repository;

	@Autowired
	RealmService service;

	@Autowired
	I18NService i18nService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	TaskProviderService taskService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {
		if (parameters.containsKey("resumeUser.name")) {
			throw new ValidationException("Username required");
		}
	}

	@Override
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		String name = processTokenReplacements(repository.getValue(task, "resumeUser.name"), event);
		try {

			if (log.isInfoEnabled()) {
				log.info("Resuming user " + name);
			}
			Principal principal = service.getUniquePrincipal(name, PrincipalType.USER);
			suspensionService.deletePrincipalSuspension(principal);
			
			suspensionService.notifyResume(principal.getPrincipalName(), false);

			return new ResumeUserResult(this, currentRealm, task,
					name);
		} catch (ResourceNotFoundException e) {
			log.error(
					"Failed to fully process resume user request for " + name,
					e);
			return new ResumeUserResult(this, e, currentRealm, task,
					name);
		}
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { ResumeUserResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
}
