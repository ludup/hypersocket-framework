package com.hypersocket.triggers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.events.EventDefinition;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface TriggerResourceService extends
		AbstractResourceService<TriggerResource> {

	TriggerResource createResource(String name,
			String event, TriggerResultType result, String task,
			Map<String, String> properties,
			Realm realm,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, 
			TriggerResource parent)
			throws ResourceCreationException, AccessDeniedException;


	TriggerResource updateResource(TriggerResource resource, String name,
			String event, TriggerResultType result, String task,
			Map<String, String> properties,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent)
			throws ResourceChangeException, AccessDeniedException;
	
	void registerConditionProvider(TriggerConditionProvider condition);

	TriggerConditionProvider getConditionProvider(TriggerCondition condition);

	List<String> getConditions();

	TriggerCondition getConditionById(Long id) throws AccessDeniedException;

	Set<String> getDefaultVariableNames();

	String getDefaultVariableValue(String variableName);

	List<EventDefinition> getTriggerEvents();

	Collection<String> getEventAttributes(String resourceKey);

	Collection<TriggerResource> getTriggersByResourceKey(String actionGenerateAlert);

	List<TriggerResource> getParentTriggers(Long id) throws ResourceNotFoundException;

	void start();

	void stop();

	Collection<String> getTasks() throws AccessDeniedException;

}
