package com.hypersocket.automation;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
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
import com.hypersocket.tasks.DynamicResultsTaskProvider;
import com.hypersocket.tasks.DynamicTaskExecutionContext;
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
	
	@Autowired
	SessionFactory sessionFactory;
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		if(!automationService.isEnabled()) {
			return;
		}
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
			
			if(resource.getFireAutomationEvents()) {
				eventService.publishEvent(event);
			}
			
			TaskResult result;
			final List<SystemEvent> sourceEvents = new ArrayList<SystemEvent>();
			sourceEvents.add(event);
			
			if(resource.getTransactional()) {
				result = transactionService.doInTransaction(new TransactionCallback<TaskResult>() {

					@Override
					public TaskResult doInTransaction(TransactionStatus status) {

						try {
							return executeTask(resource, provider, sourceEvents);
						} catch (ValidationException e) {
							throw new IllegalStateException(e.getMessage(), e);
						}
					}
					
				});
			} else {
				result = executeTask(resource, provider, sourceEvents);
			}
			
			if(resource.getFireAutomationEvents()) {
				if(result==null || result.isSuccess()) {
					eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource));
				} else {
					eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, false));
				}
			}
		} catch (Throwable e) {
			eventService.publishEvent(new AutomationTaskFinishedEvent(this, resource, e));
		}
	}

	
	private TaskResult executeTask(final AutomationResource resource, TaskProvider provider, final List<SystemEvent> sourceEvents) throws ValidationException {
		
		final SystemEvent lastEvent = sourceEvents.get(sourceEvents.size()-1);
		
		TaskResult outputEvent = null;
		if (provider instanceof DynamicResultsTaskProvider) {
			DynamicResultsTaskProvider dProvider = (DynamicResultsTaskProvider) provider;
			outputEvent = dProvider.execute(new DynamicTaskExecutionContext() {
				@Override
				public void addResults(TaskResult result) {

					/* The whole point of DynamicResultsTaskProvider is to keep memory usage during imports and 
					 * other large data tasks to a minimum. So we cannot store every single propagated
					 * event in the chain (sourceEvents), so we restrict to the current source events, 
					 * this event and its result but do not ADD to the source events. 
					 */
					List<SystemEvent> results = new ArrayList<SystemEvent>(sourceEvents);
					results.add(lastEvent);
					results.add(result.getEvent());

					if (result.isPublishable()) {
						eventService.publishEvent(result.getEvent());
					}
					try {
						for(TriggerResource trigger : resource.getChildTriggers()) {
							processEventTrigger(trigger, result.getEvent(), results);
						}
					} catch (ValidationException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}

				@Override
				public void flush() {
					log.info("Flushing");
					sessionFactory.getCurrentSession().flush();
					sessionFactory.getCurrentSession().clear();
				}

				@Override
				public boolean isTransactional() {
					return Boolean.TRUE.equals(resource.getTransactional());
				}
			}, resource, lastEvent.getCurrentRealm(), sourceEvents);
		} else if(provider != null) {
			outputEvent = provider.execute(resource, lastEvent.getCurrentRealm(), sourceEvents);
		}
		
		if(outputEvent!=null) {

			if(outputEvent instanceof MultipleTaskResults) {
				MultipleTaskResults results = (MultipleTaskResults) outputEvent;
				for(TaskResult result : results.getResults()) {
					
					sourceEvents.add(result.getEvent());
					
					if(result.isPublishable()) {
						eventService.publishEvent(result.getEvent());
					}
					
					for(TriggerResource trigger : resource.getChildTriggers()) {
						processEventTrigger(trigger, result.getEvent(), new ArrayList<SystemEvent>(sourceEvents));
					}
				}
				
			} else {

				sourceEvents.add(outputEvent.getEvent());
				
				if(outputEvent.isPublishable()) {
					eventService.publishEvent(outputEvent.getEvent());
				}
				
				for(TriggerResource trigger : resource.getChildTriggers()) {
					processEventTrigger(trigger, outputEvent.getEvent(), sourceEvents);
				}
			}
		}
		
		return outputEvent;
	}
}
