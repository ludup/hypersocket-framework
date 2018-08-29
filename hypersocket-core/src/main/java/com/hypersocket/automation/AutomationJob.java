package com.hypersocket.automation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.automation.events.AutomationTaskFinishedEvent;
import com.hypersocket.automation.events.AutomationTaskStartedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.transactions.TransactionService;
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
	ClusteredSchedulerService schedulerService; 
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	TransactionService transactionService;
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		Long resourceId = context.getTrigger().getJobDataMap().getLong("resourceId");
		Long realmId = context.getTrigger().getJobDataMap().getLong("realm");
		
		final AutomationResource resource;
		Realm realm = realmService.getRealmById(realmId);
		
		try {
			resource = automationService.getResourceById(resourceId);
			
		} catch (Exception e) {
			log.error("Could not find resource id " + resourceId + " to execute job", e);
			eventService.publishEvent(new AutomationTaskStartedEvent(this, realm, e));
			return;
		} 
		
		try {
			
			
			final TaskProvider provider = taskService.getTaskProvider(resource);
			final AutomationTaskStartedEvent event = new AutomationTaskStartedEvent(this, resource);
			
			eventService.publishEvent(event);
			
			TaskResult result;
			
			if(resource.getTransactional()) {
				result = transactionService.doInTransaction(new TransactionCallback<TaskResult>() {

					@Override
					public TaskResult doInTransaction(TransactionStatus status) {

						try {
							return executeTask(resource, provider, event);
						} catch (ValidationException e) {
							throw new IllegalStateException(e.getMessage(), e);
						}
					}
					
				});
			} else {
				result = executeTask(resource, provider, event);
			}
			
			if(result==null || result.isSuccess()) {
				eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource));
			} else {
				eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, false));
			}
		} catch (Throwable e) {
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, e));
		}
	}

	
	private TaskResult executeTask(AutomationResource resource, TaskProvider provider, SystemEvent event) throws ValidationException {
		
		if(provider==null) {
			throw new ValidationException("Task provide is no longer installed.");
		}
		
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
		
		return outputEvent;
	}
}
