package com.hypersocket.triggers;

import java.util.Collection;
import java.util.List;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tasks.TaskProvider;


public interface TriggerResourceRepository extends
		AbstractResourceRepository<TriggerResource> {

	void registerActionRepository(TaskProvider action);

	void deregisterActionRepository(TaskProvider action);

	List<TriggerResource> getTriggersForEvent(SystemEvent event);

	TriggerCondition getConditionById(Long id);

	Collection<TriggerResource> getTriggersByTask(String resourceKey);

	Collection<TriggerResource> getTriggersByEvent(String resourceKey);


}
