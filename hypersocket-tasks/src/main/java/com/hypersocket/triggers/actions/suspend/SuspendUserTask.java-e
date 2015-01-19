package com.hypersocket.triggers.actions.suspend;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
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
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class SuspendUserTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(SuspendUserTask.class);

	public static final String RESOURCE_BUNDLE = "SuspendUserTask";

	public static final String RESOURCE_KEY = "suspendUser";

	Map<String, String> suspendedUserResumeSchedules = new HashMap<String, String>();
	Set<String> suspendedUsers = new HashSet<String>();

	@Autowired
	SuspendUserTaskRepository repository;

	@Autowired
	RealmService realmService;

	@Autowired
	TriggerResourceService triggerService;

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
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {
		String name = repository.getValue(task, "suspendUser.name");
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

			if (suspendedUsers.contains(name)
					&& suspendedUserResumeSchedules.containsKey(name)) {
				if (log.isInfoEnabled()) {
					log.info(name
							+ " is already suspended. Rescheduling to new parameters");
				}

				String scheduleId = suspendedUserResumeSchedules.get(name);

				if (log.isInfoEnabled()) {
					log.info("Cancelling existing schedule for " + name);
				}

				try {
					schedulerService.cancelNow(scheduleId);
				} catch (Exception e) {
					log.error("Failed to cancel suspend schedule for " + name,
							e);
				}

			}
			Principal principal = realmService.getUniquePrincipal(name);
			realmService.createPrincipalSuspension(principal, startDate,
					duration);

			if (log.isInfoEnabled()) {
				log.info("Suspended account " + name);
			}

			suspendedUsers.add(name);

			if (duration > 0) {

				if (log.isInfoEnabled()) {
					log.info("Scheduling resume account for account " + name
							+ " in " + duration + " minutes");
				}

				PermissionsAwareJobData data = new PermissionsAwareJobData(
						event.getCurrentRealm());
				data.put("name", name);

				String scheduleId = schedulerService.scheduleIn(
						ResumeUserJob.class, data, (int) (duration * 60000));

				suspendedUserResumeSchedules.put(name, scheduleId);
			}
			return new SuspendUserResult(this, event.getCurrentRealm(), task,
					name, startDate, duration);
		} catch (SchedulerException e) {
			log.error("Failed to fully process suspend request for " + name, e);
			return new SuspendUserResult(this, e, event.getCurrentRealm(),
					task, name, startDate, duration);
		} catch (ResourceNotFoundException e) {
			log.error("Failed to fully process suspend request for " + name, e);
			return new SuspendUserResult(this, e, event.getCurrentRealm(),
					task, name, startDate, duration);
		} catch (ResourceCreationException e) {
			log.error("Failed to fully process suspend request for " + name, e);
			return new SuspendUserResult(this, e, event.getCurrentRealm(),
					task, name, startDate, duration);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	public void notifyResume(String name, boolean onSchedule) {

		suspendedUsers.remove(name);
		String scheduleId = suspendedUserResumeSchedules.remove(name);

		if (!onSchedule && scheduleId != null) {
			try {
				schedulerService.cancelNow(scheduleId);
			} catch (SchedulerException e) {
				log.error(
						"Failed to cancel resume job for user "
								+ name.toString(), e);
			}
		}

	}

}
