package com.hypersocket.tasks.system;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.ValidationException;

@Component
public class SystemTriggerActionImpl extends AbstractTaskProvider {

	public static final String RESOURCE_BUNDLE = "SystemTriggerAction";
	
	public static final String ACTION_SHUTDOWN = "shutdownAction";
	public static final String ACTION_RESTART = "restartAction";
	
	@Autowired
	SystemTriggerActionRepository repository; 
	
	@Autowired
	HypersocketServer server;
	
	@Autowired
	TriggerResourceService triggerService; 
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	TaskProviderService taskService; 
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
	}
	
	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_SHUTDOWN, ACTION_RESTART} ;
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {
		
	}

	@Override
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		Long delay = repository.getLongValue(task, "operation.delay");
		
		if(task.getResourceKey().equals(ACTION_SHUTDOWN)) {		
			server.shutdown(delay);
		} else if(task.getResourceKey().equals(ACTION_RESTART)) {		
			server.restart(delay);
		} else {
			throw new ValidationException("Invalid resource key for system trigger action");
		}
		
 		return null;
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
}
