package com.hypersocket.triggers.actions.system;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.triggers.AbstractActionProvider;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;
import com.hypersocket.triggers.TriggerActionProvider;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.ValidationException;

@Component
public class SystemTriggerActionImpl extends AbstractActionProvider implements TriggerActionProvider {

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
	
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		triggerService.registerActionProvider(this);
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
	public void validate(TriggerAction action, Map<String, String> parameters)
			throws ValidationException {
		
	}

	@Override
	public ActionResult execute(TriggerAction action, SystemEvent event)
			throws ValidationException {

		Long delay = repository.getLongValue(action, "operation.delay");
		
		if(action.getResourceKey().equals(ACTION_SHUTDOWN)) {		
			server.shutdown(delay);
		} else if(action.getResourceKey().equals(ACTION_RESTART)) {		
			server.restart(delay);
		} else {
			throw new ValidationException("Invalid resource key for system trigger action");
		}
		
 		return null;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	@Override
	public String[] getRequiredAttributes() {
		return new String[] { "operation.delay"};
	}

	
}
