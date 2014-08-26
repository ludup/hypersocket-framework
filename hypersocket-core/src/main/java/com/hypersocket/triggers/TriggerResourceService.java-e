package com.hypersocket.triggers;

import java.util.List;
import java.util.Set;

import com.hypersocket.events.EventDefinition;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface TriggerResourceService extends
		AbstractResourceService<TriggerResource> {

	TriggerResource updateResource(TriggerResource resourceById, String name, String event, TriggerResultType result,
			List<TriggerCondition> allConditions, List<TriggerCondition> anyConditions, List<TriggerAction> actions)
			throws ResourceChangeException, AccessDeniedException;

	TriggerResource createResource(String name, String event, TriggerResultType result, Realm realm,
			List<TriggerCondition> allConditions, List<TriggerCondition> anyConditions, List<TriggerAction> actions)
			throws ResourceCreationException, AccessDeniedException;

	void registerActionProvider(TriggerActionProvider action);

	void registerConditionProvider(TriggerConditionProvider condition);

	TriggerConditionProvider getConditionProvider(TriggerCondition condition);

	TriggerActionProvider getActionProvider(String resourceKey);

	List<String> getConditions();

	List<String> getActions();

	TriggerAction getActionById(Long id) throws AccessDeniedException;

	TriggerCondition getConditionById(Long id) throws AccessDeniedException;

	Set<String> getDefaultVariableNames();

	String getDefaultVariableValue(String variableName);

	List<EventDefinition> getTriggerEvents();

}
