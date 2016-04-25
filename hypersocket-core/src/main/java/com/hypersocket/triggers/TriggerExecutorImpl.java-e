package com.hypersocket.triggers;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SynchronousEvent;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;

@Component
public class TriggerExecutorImpl extends AbstractAuthenticatedServiceImpl implements TriggerExecutor {

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
	
	@Autowired
	SchedulerService schedulerService; 
	
	@Autowired
	ConfigurationService configurationService; 
	
	@Autowired
	RealmService realmService;
	
	public TriggerExecutorImpl() {
	}
	
	@Override
	public void scheduleOrExecuteTrigger(TriggerResource trigger, SystemEvent sourceEvent) throws ValidationException {
		
		if(sourceEvent instanceof SynchronousEvent || sourceEvent instanceof TaskResultCallback) {
			try {
				if(log.isInfoEnabled()) {
					log.info(String.format("Processing synchronous event %s with trigger %s", 
							sourceEvent.getResourceKey(), trigger.getName()));
				}
				processEventTrigger(trigger, sourceEvent, sourceEvent);
			} catch (ValidationException e) {
				log.error("Trigger failed validation", e);
			}
			
		} else {
		
			Principal principal = realmService.getSystemPrincipal();
			
			if(sourceEvent.hasAttribute(CommonAttributes.ATTR_PRINCIPAL_NAME)) {
				principal = realmService.getPrincipalByName(
						sourceEvent.getCurrentRealm(), 
						sourceEvent.getAttribute(CommonAttributes.ATTR_PRINCIPAL_NAME),
						PrincipalType.USER);
			} else if(hasAuthenticatedContext()) {
				principal = getCurrentPrincipal();
			}
			
			JobDataMap data = new PermissionsAwareJobData(
					sourceEvent.getCurrentRealm(),
					principal,
					hasAuthenticatedContext() ? getCurrentLocale()
							: configurationService.getDefaultLocale(),
					"triggerExecutionJob");

			data.put("event", sourceEvent);
			data.put("sourceEvent", sourceEvent);
			data.put("trigger", trigger);
			data.put("realm", sourceEvent.getCurrentRealm());
			
			try {
				schedulerService.scheduleNow(TriggerJob.class, data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule event trigger job", e);
			}
		}
	}

	@Override
	public void processEventTrigger(TriggerResource trigger,
			SystemEvent result, SystemEvent sourceEvent) throws ValidationException {
		
		if (log.isInfoEnabled()) {
			log.info("Processing trigger " + trigger.getName());
		}
		
		if(trigger.getResult() != TriggerResultType.EVENT_ANY_RESULT
				&& result.getStatus().ordinal() != trigger.getResult().ordinal()) {
			if(log.isInfoEnabled()) {
				log.info("Not processing trigger " + trigger.getName() + " with result " + trigger.getResult().toString()
						+ " because event status is " + result.getStatus().toString());
			}
			return;
		}
		
		if (checkConditions(trigger, result)) {
			
			if(log.isInfoEnabled()) {
				log.info("Trigger conditions match. Performing task for " + trigger.getName() + " [" + trigger.getResourceKey() + "]");
			}
			executeTrigger(trigger, result, sourceEvent);
			
		} else {
			if(log.isInfoEnabled()) {
				log.info("Not processing trigger " 
						+ trigger.getName() 
						+ " because its conditions do not match the current event attributes");
			}
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

	protected void executeTrigger(TriggerResource trigger, SystemEvent lastResult, SystemEvent sourceEvent)
			throws ValidationException {

		TaskProvider provider = taskService
				.getTaskProvider(trigger.getResourceKey());
		if (provider == null) {
			throw new ValidationException(
					"Failed to execute task because provider "
							+ trigger.getResourceKey() + " is not available");
		}

		TaskResult outputEvent = provider.execute(trigger, lastResult.getCurrentRealm(), lastResult);

		if(outputEvent!=null) {
			
			if(outputEvent instanceof MultipleTaskResults) {
				MultipleTaskResults results = (MultipleTaskResults) outputEvent;
				for(TaskResult result : results.getResults()) {
					
					if(result.isPublishable()) {
						eventService.publishEvent(result.getEvent());
					}
					
					processResult(trigger, result, sourceEvent);
				}
				
			} else {
			
				if(outputEvent.isPublishable()) {
					eventService.publishEvent(outputEvent.getEvent());
				}
				
				processResult(trigger, outputEvent, sourceEvent);
			}
		}
	}
	
	protected void processResult(TriggerResource trigger, TaskResult result, SystemEvent sourceEvent) throws ValidationException {
		
		if(sourceEvent instanceof TaskResultCallback) {
			TaskResultCallback callbackEvent = (TaskResultCallback) sourceEvent;
			try {
				callbackEvent.processResult(result);
			} catch (Throwable e) {
			}
		}
		
		if (!trigger.getChildTriggers().isEmpty()) {
			for(TriggerResource t : trigger.getChildTriggers()) {
				processEventTrigger(t, result.getEvent(), sourceEvent);
			}
			
		} 
	}
}

