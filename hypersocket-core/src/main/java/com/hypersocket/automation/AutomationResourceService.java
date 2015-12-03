package com.hypersocket.automation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResultType;

public interface AutomationResourceService extends
		AbstractResourceService<AutomationResource> {

	AutomationResource updateResource(AutomationResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	AutomationResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(AutomationResource resource)
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(String resourceKey)
			throws AccessDeniedException;

	Collection<String> getTasks() throws AccessDeniedException;

	void scheduleDailyJobs();

	AutomationResource createTrigger(String name, String event, TriggerResultType result, String task,
			Map<String, String> properties, Realm realm, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent, AutomationResource resource)
					throws ResourceCreationException, AccessDeniedException;

	AutomationResource updateTrigger(TriggerResource resource, String name, String event, TriggerResultType result,
			String task, Map<String, String> properties, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions, TriggerResource parent, AutomationResource resource2)
					throws ResourceChangeException, AccessDeniedException;

	void runNow(AutomationResource resource) throws SchedulerException;

}
