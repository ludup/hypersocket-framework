package com.hypersocket.tasks.alert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.alert.AlertCallback;
import com.hypersocket.alert.AlertService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.ValidationException;

@Component
public class AlertTask extends AbstractTaskProvider {

	public static final String ACTION_GENERATE_ALERT = "generateAlert";

	public static final String ATTR_KEY = "alert.key";
	public static final String ATTR_THRESHOLD = "alert.threshold";
	public static final String ATTR_TIMEOUT = "alert.timeout";
	public static final String ATTR_RESET_DELAY = "alert.reset";
	public static final String ATTR_ALERT_ID = "alert.id";

	@Autowired
	private AlertTaskRepository taskRepository; 

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService; 

	@Autowired
	private AlertService alertService; 
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		eventService.registerEvent(AlertEvent.class,
				TaskProviderServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_GENERATE_ALERT };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public AbstractTaskResult execute(final Task task, 
			final Realm currentRealm, 
			final List<SystemEvent> event)
			throws ValidationException {

		final SystemEvent lastEvent = event.get(event.size()-1);
		StringBuffer key = new StringBuffer();

		for (String attr : ResourceUtils.explodeValues(taskRepository.getValue(task,
				ATTR_KEY))) {
			if (key.length() > 0) {
				key.append("|");
			}
			key.append(lastEvent.getAttribute(attr));
		}

		final int threshold = taskRepository.getIntValue(task, ATTR_THRESHOLD);
		final int timeout = taskRepository.getIntValue(task, ATTR_TIMEOUT);
		final int delay = taskRepository.getIntValue(task,  ATTR_RESET_DELAY);
		
		String alertKey = key.toString();
		
		
		AbstractTaskResult result = alertService.processAlert(task.getResourceKey(), alertKey, delay, threshold, timeout, new AlertCallback<AlertEvent>() {

			@Override
			public AlertEvent alert() {
				return new AlertEvent(AlertTask.this, AlertEvent.EVENT_RESOURCE_KEY, true, currentRealm, threshold, timeout, task, lastEvent);
			}
			
		});
		
		if(Objects.nonNull(result)) {
			return result;
		} else {
			return new AlertEvent(this, task, lastEvent);
		}
	}
	
	public String getResultResourceKey() {
		return AlertEvent.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
	}

	@Override
	public void taskCreated(Task task) {
		alertService.registerDynamicEvent((TriggerResource)task);
	}

	@Override
	public void taskUpdated(Task task) {
		alertService.registerDynamicEvent((TriggerResource)task);
	}

	@Override
	public void taskDeleted(Task task) {
		
	}

	@Override
	public boolean supportsAutomation() {
		return false;
	}
	
	@Override
	public boolean supportsTriggers() {
		return true;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
