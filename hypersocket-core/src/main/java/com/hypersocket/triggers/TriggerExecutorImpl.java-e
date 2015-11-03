package com.hypersocket.triggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;

@Component
public class TriggerExecutorImpl implements TriggerExecutor {

	static Logger log = LoggerFactory.getLogger(TriggerExecutor.class);
	
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
	
	public TriggerExecutorImpl() {
	}

	@Override
	public void processEventTrigger(TriggerResource trigger,
			SystemEvent event) throws ValidationException {
		
		if (log.isInfoEnabled()) {
			log.info("Processing trigger " + trigger.getName());
		}
		
		if(trigger.getResult() != TriggerResultType.EVENT_ANY_RESULT
				&& event.getStatus().ordinal() != trigger.getResult().ordinal()) {
			if(log.isInfoEnabled()) {
				log.info("Not processing trigger " + trigger.getName() + " with result " + trigger.getResult().toString()
						+ " because event status is " + event.getStatus().toString());
			}
			return;
		}
		
		if (checkConditions(trigger, event)) {
			
			if(log.isInfoEnabled()) {
				log.info("Performing task " + trigger.getResourceKey());
			}
			executeTrigger(trigger, event);
			
		}
		if (log.isInfoEnabled()) {
			log.info("Finished processing trigger " + trigger.getName());
		}

	}

	protected boolean checkConditions(TriggerResource trigger, SystemEvent event)
			throws ValidationException {

		for (TriggerCondition condition : trigger.getAllConditions()) {
			if (!checkCondition(condition, trigger, event)) {
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
				if (checkCondition(condition, trigger, event)) {
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
			TriggerResource trigger, SystemEvent event)
			throws ValidationException {

		TriggerConditionProvider provider = triggerService
				.getConditionProvider(condition);

		if (provider == null) {
			throw new ValidationException(
					"Failed to check condition because provider "
							+ condition.getConditionKey() + " is not available");
		}
		
		return provider.checkCondition(condition, trigger, event);
	}

	protected void executeTrigger(TriggerResource trigger, SystemEvent event)
			throws ValidationException {

		TaskProvider provider = taskService
				.getTaskProvider(trigger.getResourceKey());
		if (provider == null) {
			throw new ValidationException(
					"Failed to execute task because provider "
							+ trigger.getResourceKey() + " is not available");
		}

		TaskResult outputEvent = provider.execute(trigger, event.getCurrentRealm(), event);

		if(outputEvent!=null) {
			if(outputEvent.isPublishable()) {
				eventService.publishEvent(outputEvent);
			}
			
			if(outputEvent instanceof MultipleTaskResults) {
				MultipleTaskResults results = (MultipleTaskResults) outputEvent;
				for(TaskResult result : results.getResults()) {
					processResult(trigger, result);
				}
				
			} else {
				processResult(trigger, outputEvent);
			}
		}
	}
	
	protected void processResult(TriggerResource trigger, TaskResult result) throws ValidationException {
		
		if (!trigger.getChildTriggers().isEmpty()) {
			for(TriggerResource t : trigger.getChildTriggers()) {
				processEventTrigger(t, result);
			}
			
		} 
	}
}

