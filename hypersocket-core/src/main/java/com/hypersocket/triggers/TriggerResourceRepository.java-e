package com.hypersocket.triggers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tasks.TaskProvider;


public interface TriggerResourceRepository extends
		AbstractResourceRepository<TriggerResource> {

	void registerActionRepository(TaskProvider action);

	List<TriggerResource> getTriggersForEvent(SystemEvent event);

	TriggerAction getActionById(Long id);

	TriggerCondition getConditionById(Long id);

	void updateResource(TriggerResource resource, Map<String, String> properties);

	Collection<TriggerAction> getActionsByResourceKey(String resourceKey);

}
