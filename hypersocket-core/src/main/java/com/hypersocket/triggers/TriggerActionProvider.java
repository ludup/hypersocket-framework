package com.hypersocket.triggers;

import java.util.Collection;
import java.util.Map;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;

public interface TriggerActionProvider {

	String getResourceBundle();
	
	String[] getResourceKeys();

	void validate(TriggerAction action, Map<String, String> parameters)
			throws TriggerValidationException;

	ActionResult execute(TriggerAction action, SystemEvent event)
			throws TriggerValidationException;
	
	ResourceTemplateRepository getRepository();

	String[] getRequiredAttributes();

	Collection<PropertyCategory> getPropertyTemplate();

	Collection<PropertyCategory> getPropertiesForAction(TriggerAction action);
	
	void actionCreated(TriggerAction action);
	
	void actionUpdated(TriggerAction action);
	
	void actionDeleted(TriggerAction action);
}
