package com.hypersocket.automation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.automation.events.AutomationTaskFinishedEvent;
import com.hypersocket.automation.events.AutomationTaskStartedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.AbstractTriggerJob;
import com.hypersocket.triggers.MultipleTaskResults;
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
	
	@Autowired
	RealmService realmService;
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		Long resourceId = context.getTrigger().getJobDataMap().getLong("resourceId");
		Long realmId = context.getTrigger().getJobDataMap().getLong("realm");
		
		AutomationResource resource;
		Realm realm = null;
		try {
			realm = realmService.getRealmById(realmId);
		} catch (AccessDeniedException e) {
			log.error("Could not find realm id " + resourceId + " to execute job", e);
		}
		
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
			
			
			TaskResult outputEvent = provider.execute(resource, event.getCurrentRealm(), event);
			
			if(outputEvent!=null) {

				if(outputEvent instanceof MultipleTaskResults) {
					MultipleTaskResults results = (MultipleTaskResults) outputEvent;
					for(TaskResult result : results.getResults()) {
						
						if(result.isPublishable()) {
							eventService.publishEvent(result.getEvent());
						}
						
						for(TriggerResource trigger : resource.getChildTriggers()) {
							processEventTrigger(trigger, result.getEvent(), event);
						}
					}
					
				} else {

					if(outputEvent.isPublishable()) {
						eventService.publishEvent(outputEvent.getEvent());
					}
					
					for(TriggerResource trigger : resource.getChildTriggers()) {
						processEventTrigger(trigger, outputEvent.getEvent(), event);
					}
				}
			}
			
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource));
		} catch (ValidationException e) {
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, e));
		}
	}

}
