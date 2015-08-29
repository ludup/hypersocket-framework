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
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTriggerJob;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public class AutomationJob extends AbstractTriggerJob {

	static Logger log = LoggerFactory.getLogger(AutomationJob.class);
	
	@Autowired
	AutomationResourceService automationService; 
	
	@Autowired
	TaskProviderService taskService;
	
	@Autowired
	EventService eventService; 
	
	@Autowired
	SchedulerService schedulerService; 
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		PermissionsAwareJobData jobdata = (PermissionsAwareJobData) context.getTrigger().getJobDataMap();
		
		Long resourceId = context.getTrigger().getJobDataMap().getLong("resourceId");
		Realm realm = (Realm) context.getTrigger().getJobDataMap().get("realm");
		
		AutomationResource resource;
		
		try {
			resource = automationService.getResourceById(resourceId);
		} catch (Exception e) {
			log.error("Could not find resource id " + resourceId + " to execute job", e);
			eventService.publishEvent(new AutomationTaskStartedEvent(this, realm, e));
			return;
		} 
		
		try {
			
			
			TaskProvider provider = taskService.getTaskProvider(resource);
			
			AutomationTaskStartedEvent event = new AutomationTaskStartedEvent(this, resource);
			
			eventService.publishEvent(event);
			
			TaskResult result = provider.execute(resource, event.getCurrentRealm(), event);
			
			if(result!=null && result.isPublishable()) {
				eventService.publishEvent(result);
			}
			
			for(TriggerResource trigger : resource.getChildTriggers()) {
				processEventTrigger(trigger, result);
			}
			
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource));
		} catch (ValidationException e) {
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, e));
		}
	}

}
