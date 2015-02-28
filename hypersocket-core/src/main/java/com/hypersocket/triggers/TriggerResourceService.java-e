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

public interface TriggerResourceService extends
		AbstractResourceService<TriggerResource> {

	TriggerResource updateResource(TriggerResource resourceById, String name,
			String event, TriggerResultType result,
			Map<String, String> properties,
			List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, List<TriggerAction> actions,
			TriggerAction parentAction)
			throws ResourceChangeException, AccessDeniedException;

	TriggerResource createResource(String name, String event,
			TriggerResultType result, Map<String, String> properties,
			Realm realm, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, List<TriggerAction> actions,
			TriggerAction parentAction)
			throws ResourceCreationException, AccessDeniedException;

	void registerConditionProvider(TriggerConditionProvider condition);

	TriggerConditionProvider getConditionProvider(TriggerCondition condition);

	List<String> getConditions();

	TriggerAction getActionById(Long id) throws AccessDeniedException;

	TriggerCondition getConditionById(Long id) throws AccessDeniedException;

	Set<String> getDefaultVariableNames();

	String getDefaultVariableValue(String variableName);

	List<EventDefinition> getTriggerEvents();

	Collection<String> getEventAttributes(String resourceKey);

	Collection<TriggerAction> getActionsByResourceKey(String actionGenerateAlert);

	List<TriggerResource> getParentTriggers(Long id);

}
