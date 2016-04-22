package com.hypersocket.tasks.sleep;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class SleepTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(SleepTask.class);
	
	public static final String TASK_RESOURCE_KEY = "sleepTask";

	public static final String RESOURCE_BUNDLE = "SleepTask";
	
	@Autowired
	SleepTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	public SleepTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(SleepTaskResult.class,
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
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		Long sleepTime = repository.getLongValue(task, "sleep.milli");
		try {
			Thread.sleep(sleepTime);
			return new SleepTaskResult(this, true, currentRealm, task, sleepTime);
		} catch (InterruptedException e) {
			return new SleepTaskResult(this, e, currentRealm, task, sleepTime);
		}
		
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { SleepTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
