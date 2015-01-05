package com.hypersocket.automation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.automation.events.AutomationTaskFinishedEvent;
import com.hypersocket.automation.events.AutomationTaskStartedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.ValidationException;

public class AutomationJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(AutomationJob.class);
	
	@Autowired
	AutomationResourceService automationService; 
	
	@Autowired
	TaskProviderService taskService;
	
	@Autowired
	EventService eventService; 
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		
		Long resourceId = context.getTrigger().getJobDataMap().getLong("resourceId");
		Realm realm = (Realm) context.getTrigger().getJobDataMap().get("realm");
		
		AutomationResource resource;
		
		try {
		resource = automationService.getResourceById(resourceId);
		} catch (ResourceNotFoundException e) {
			log.error("Could not find resource id " + resourceId + " to execute job", e);
			eventService.publishEvent(new AutomationTaskStartedEvent(this, realm, e));
			return;
		} 
		
		try {
			
			
			TaskProvider provider = taskService.getActionProvider(resource);
			
			AutomationTaskStartedEvent event = new AutomationTaskStartedEvent(this, resource);
			
			eventService.publishEvent(event);
			
			provider.execute(resource, event);
			
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource));
		} catch (ValidationException e) {
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, e));
		}
	}

}
