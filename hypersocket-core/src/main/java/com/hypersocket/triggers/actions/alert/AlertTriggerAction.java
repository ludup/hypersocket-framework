package com.hypersocket.triggers.actions.alert;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.triggers.AbstractActionProvider;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;
import com.hypersocket.triggers.TriggerActionProvider;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerValidationException;

@Component
public class AlertTriggerAction extends AbstractActionProvider implements
		TriggerActionProvider {

	public static final String ACTION_GENERATE_ALERT = "generateAlert";
	
	@Autowired
	AlertTriggerActionRepository repository;
	
	@Autowired
	TriggerResourceService triggerService;
	
	@PostConstruct
	private void postConstruct() {
		triggerService.registerActionProvider(this);
	}
	
	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] {ACTION_GENERATE_ALERT};
	}

	@Override
	public void validate(TriggerAction action, Map<String, String> parameters)
			throws TriggerValidationException {
		
	}

	@Override
	public ActionResult execute(TriggerAction action, SystemEvent event)
			throws TriggerValidationException {

		return null;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	@Override
	public String[] getRequiredAttributes() {
		return new String[] {} ;
	}



}
