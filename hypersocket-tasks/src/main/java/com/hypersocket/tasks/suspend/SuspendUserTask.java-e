package com.hypersocket.tasks.suspend;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class SuspendUserTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(SuspendUserTask.class);

	public static final String RESOURCE_BUNDLE = "SuspendUserTask";

	public static final String RESOURCE_KEY = "suspendUser";

	@Autowired
	SuspendUserTaskRepository repository;

	@Autowired
	PrincipalSuspensionService suspensionService; 
	
	@Autowired
	RealmService realmService;

	@Autowired
	I18NService i18nService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);

		eventService.registerEvent(SuspendUserResult.class, RESOURCE_BUNDLE);
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
		if (parameters.containsKey("suspendUser.name")) {
			throw new ValidationException("Name required");
		} else if (parameters.containsKey("suspendUser.duration")) {
			throw new ValidationException("Duration required");
		}
	}

	@Override
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {
		String name = processTokenReplacements(repository.getValue(task, "suspendUser.name"), event);
		Long duration = repository.getLongValue(task, "suspendUser.duration");
		Date startDate = ((AutomationResource) task).getStartDate();
		String startTime = ((AutomationResource) task).getStartTime();
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY,
					Integer.valueOf(startTime.split(":")[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(startTime.split(":")[1]));
			startDate = cal.getTime();
		} else {
			startDate = new Date();
		}

		try {

			if (log.isInfoEnabled()) {
				log.info("Suspending account " + name);
			}

			Principal principal = realmService.getUniquePrincipal(name, PrincipalType.USER);
			suspensionService.createPrincipalSuspension(principal, startDate, duration);

			if (log.isInfoEnabled()) {
				log.info("Suspended account " + name);
			}

			return new SuspendUserResult(this, currentRealm, task,
					name, startDate, duration);
		} catch (ResourceException e) {
			log.error("Failed to fully process suspend request for " + name, e);
			return new SuspendUserResult(this, e, currentRealm,
					task, name, startDate, duration);
		} 
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { SuspendUserResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
