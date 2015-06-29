package com.hypersocket.triggers;

import org.apache.commons.lang3.ArrayUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.events.TriggerExecutedEvent;

public class TriggerJob extends PermissionsAwareJob {

	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	AuthenticationService authenticationService; 
	
	@Autowired
	I18NService i18nService; 
	
	@Autowired
	EventService eventService; 
	
	@Autowired
	TaskProviderService taskService; 
	
	static Logger log = LoggerFactory.getLogger(TriggerJob.class);

	@Override
	public void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		SystemEvent event = (SystemEvent) context.getTrigger().getJobDataMap().get("event");
		
		if(log.isInfoEnabled()) {
			log.info("Starting trigger job for event " + event.getResourceKey());
		}
	
		TriggerResource trigger = (TriggerResource) context.getTrigger()
				.getJobDataMap().get("trigger");

		try {
			processEventTrigger(trigger, event);
		} catch (Throwable e) {
			eventService.publishEvent(new TriggerExecutedEvent(this, trigger, e));
		} 

	}

	protected void processEventTrigger(TriggerResource trigger,
			SystemEvent... events) throws ValidationException {
		if (log.isInfoEnabled()) {
			log.info("Processing trigger " + trigger.getName());
		}
		
		if(trigger.getResult() != TriggerResultType.EVENT_ANY_RESULT
				&& events[events.length-1].getStatus().ordinal() != trigger.getResult().ordinal()) {
			if(log.isInfoEnabled()) {
				log.info("Not processing trigger " + trigger.getName() + " with result " + trigger.getResult().toString()
						+ " because event status is " + events[events.length-1].getStatus().toString());
			}
			return;
		}
		
		if (checkConditions(trigger, events)) {
			
			if(log.isInfoEnabled()) {
				log.info("Performing task " + trigger.getResourceKey());
			}
			executeTrigger(trigger, events);
			
		}
		if (log.isInfoEnabled()) {
			log.info("Finished processing trigger " + trigger.getName());
		}

	}

	protected boolean checkConditions(TriggerResource trigger, SystemEvent... events)
			throws ValidationException {

		for (TriggerCondition condition : trigger.getAllConditions()) {
			if (!checkCondition(condition, trigger, events)) {
				if (log.isDebugEnabled()) {
					log.debug("Trigger " + trigger.getName()
							+ " failed processing all conditions due to "
							+ condition.getConditionKey() + " attributeValue="
							+ condition.getAttributeKey() + " conditionValue="
							+ condition.getConditionValue());
				}
				return false;
			}
		}

		if (trigger.getAnyConditions().size() > 0) {
			boolean conditionPassed = false;
			for (TriggerCondition condition : trigger.getAnyConditions()) {
				if (checkCondition(condition, trigger, events)) {
					conditionPassed = true;
					break;
				}
				if (log.isDebugEnabled()) {
					log.debug("Trigger " + trigger.getName()
							+ " failed processing any conditions due to "
							+ condition.getConditionKey() + " attributeValue="
							+ condition.getAttributeKey() + " conditionValue="
							+ condition.getConditionValue());
				}
			}
			return conditionPassed;
		}

		return true;
	}

	private boolean checkCondition(TriggerCondition condition,
			TriggerResource trigger, SystemEvent... events)
			throws ValidationException {

		TriggerConditionProvider provider = triggerService
				.getConditionProvider(condition);

		if (provider == null) {
			throw new ValidationException(
					"Failed to check condition because provider "
							+ condition.getConditionKey() + " is not available");
		}
		
		boolean matched = false;
		for(SystemEvent event : events) {
			matched |= provider.checkCondition(condition, trigger, event);
		}
		return matched;
	}

	protected void executeTrigger(TriggerResource trigger, SystemEvent... events)
			throws ValidationException {

		TaskProvider provider = taskService
				.getTaskProvider(trigger.getResourceKey());
		if (provider == null) {
			throw new ValidationException(
					"Failed to execute task because provider "
							+ trigger.getResourceKey() + " is not available");
		}

		TaskResult outputEvent = provider.execute(trigger, events[0].getCurrentRealm(), events);

		if(outputEvent!=null) {
			if(outputEvent.isPublishable()) {
				eventService.publishEvent(outputEvent);
			}
			
			SystemEvent[] allEvents = ArrayUtils.add(events, outputEvent);
			
			if (!trigger.getChildTriggers().isEmpty()) {
				for(TriggerResource t : trigger.getChildTriggers()) {
					processEventTrigger(t, allEvents);
				}
				
			} 
		}
		
	}
}
