package com.hypersocket.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SynchronousEvent;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.LocalSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.tasks.DynamicResultsTaskProvider;
import com.hypersocket.tasks.DynamicTaskExecutionContext;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.transactions.TransactionService;
import com.hypersocket.triggers.conditions.TriggerAttributeHelper;

@Component
public class TriggerExecutorImpl extends AbstractAuthenticatedServiceImpl implements TriggerExecutor {

	static Logger log = LoggerFactory.getLogger(TriggerExecutor.class);

	@Autowired
	private TriggerResourceService triggerService;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private LocalSchedulerService schedulerService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private SessionFactory sessionFactory;

	public TriggerExecutorImpl() {
	}

	@Override
	public void scheduleOrExecuteTrigger(TriggerResource trigger, List<SystemEvent> sourceEvents)
			throws ValidationException {

		var sourceEvent = sourceEvents.get(sourceEvents.size() - 1);

		if (sourceEvent instanceof SynchronousEvent || sourceEvent instanceof TaskResultCallback) {
			try {
				if (log.isInfoEnabled()) {
					log.info(String.format("Processing synchronous event %s with trigger %s",
							sourceEvent.getResourceKey(), trigger.getName()));
				}
				processEventTrigger(trigger, sourceEvent, sourceEvents);
			} catch (ValidationException e) {
				log.error("Trigger failed validation", e);
			}

		} else {

			var principal = realmService.getSystemPrincipal();

			var currentRealm = sourceEvent.getCurrentRealm();
			if (sourceEvent.hasAttribute(CommonAttributes.ATTR_PRINCIPAL_NAME)) {
				principal = realmService.getPrincipalByName(currentRealm,
						sourceEvent.getAttribute(CommonAttributes.ATTR_PRINCIPAL_NAME), PrincipalType.USER);
			} else if (hasAuthenticatedContext()) {
				principal = getCurrentPrincipal();
			}

			var data = new PermissionsAwareJobData(currentRealm, principal,
					hasAuthenticatedContext() ? getCurrentLocale() : configurationService.getDefaultLocale(),
					"triggerExecutionJob", trigger.getName());

			data.put("event", sourceEvent);
			data.put("sourceEvent", sourceEvents);
			data.put("trigger", trigger);

			try {
				var scheduleId = UUID.randomUUID().toString();

				schedulerService.scheduleNow(TriggerJob.class, scheduleId, data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule event trigger job", e);
			}
		}
	}

	@Override
	public void processEventTrigger(TriggerResource trigger, SystemEvent result, List<SystemEvent> sourceEvents)
			throws ValidationException {

		if (log.isInfoEnabled()) {
			log.info("Processing trigger " + trigger.getName());
		}

		if(!trigger.getAllRealms()) {
			if(!trigger.getRealm().equals(getCurrentRealm())) {
				log.info("Trigger {} is not executable in this realm {} [{}]", trigger.getName(), getCurrentRealm().getName(), trigger.getRealm().getName());
				return;
			}
		}
		
		if (trigger.getResult() != TriggerResultType.EVENT_ANY_RESULT
				&& result.getStatus().ordinal() != trigger.getResult().ordinal()) {
			if (log.isInfoEnabled()) {
				log.info("Not processing trigger " + trigger.getName() + " with result "
						+ trigger.getResult().toString() + " because event status is " + result.getStatus().toString());
			}
			return;
		}

		if (checkConditions(trigger, result, sourceEvents)) {

			if (log.isInfoEnabled()) {
				log.info("Trigger conditions match. Performing task for " + trigger.getName() + " ["
						+ trigger.getResourceKey() + "]");
			}
			executeTrigger(trigger, result, sourceEvents);

		} else {
			if (log.isInfoEnabled()) {
				log.info("Not processing trigger " + trigger.getName()
						+ " because its conditions do not match the current event attributes");
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Finished processing trigger " + trigger.getName());
		}

	}

	protected boolean checkConditions(TriggerResource trigger, SystemEvent event, List<SystemEvent> sourceEvents)
			throws ValidationException {

		for (TriggerCondition condition : trigger.getAllConditions()) {
			if (!checkCondition(condition, trigger, event, sourceEvents)) {
				if (log.isDebugEnabled()) {
					log.debug("Trigger " + trigger.getName() + " failed processing all conditions due to "
							+ condition.getConditionKey() + " attributeKey=" + condition.getAttributeKey()
							+ " attributeValue=" + TriggerAttributeHelper.getAttribute(condition.getAttributeKey(), sourceEvents) + " conditionValue=" + condition.getConditionValue());
				}
				return false;
			}
		}

		if (trigger.getAnyConditions().size() > 0) {
			boolean conditionPassed = false;
			for (TriggerCondition condition : trigger.getAnyConditions()) {
				if (checkCondition(condition, trigger, event, sourceEvents)) {
					conditionPassed = true;
					break;
				}
				if (log.isDebugEnabled()) {
					log.debug("Trigger " + trigger.getName() + " failed processing any conditions due to "
							+ condition.getConditionKey() + " attributeKey=" + condition.getAttributeKey() +
							" attributeValue=" + TriggerAttributeHelper.getAttribute(condition.getAttributeKey(), sourceEvents) + " conditionValue=" + condition.getConditionValue());
				}
			}
			return conditionPassed;
		}

		return true;
	}

	private boolean checkCondition(TriggerCondition condition, TriggerResource trigger, SystemEvent event,
			List<SystemEvent> sourceEvents) throws ValidationException {

		TriggerConditionProvider provider = triggerService.getConditionProvider(condition);

		if (provider == null) {
			throw new ValidationException(
					"Failed to check condition because provider " + condition.getConditionKey() + " is not available");
		}

		return provider.checkCondition(condition, trigger, event, sourceEvents);
	}

	protected void executeTrigger(final TriggerResource trigger, final SystemEvent triggeredEvent,
			final List<SystemEvent> sourceEvents) throws ValidationException {

		if(!triggerService.isEnabled()) {
			return;
		}
		
		final TaskProvider provider = taskService.getTaskProvider(trigger.getResourceKey());
		if (provider == null) {
			throw new ValidationException(
					"Failed to execute task because provider " + trigger.getResourceKey() + " is not available");
		}

		try {
			TaskResult outputEvent = transactionService.doInTransaction(new TransactionCallback<TaskResult>() {

				@Override
				public TaskResult doInTransaction(TransactionStatus status) {
					try {
						if (provider instanceof DynamicResultsTaskProvider) {
							
							DynamicResultsTaskProvider dProvider = (DynamicResultsTaskProvider) provider;
							return dProvider.execute(new DynamicTaskExecutionContext() {
								@Override
								public void addResults(TaskResult result) {


									/* The whole point of DynamicResultsTaskProvider is to keep memory usage during imports and 
									 * other large data tasks to a minimum. So we cannot store every single propagated
									 * event in the chain (sourceEvents), so we restrict to this event and its result. 
									 */
									List<SystemEvent> results = new ArrayList<SystemEvent>();
									results.add(triggeredEvent);
									results.add(result.getEvent());


									if (result.isPublishable()) {
										eventService.publishEvent(result.getEvent());
									}
									try {
										processResult(trigger, triggeredEvent, result, results);
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
									return true;
								}
							}, trigger, triggeredEvent.getCurrentRealm(), sourceEvents);
						} else {
							return provider.execute(trigger, triggeredEvent.getCurrentRealm(), sourceEvents);
						}
					} catch (ValidationException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}

			});

			if (outputEvent != null) {

				if (outputEvent instanceof MultipleTaskResults) {
					MultipleTaskResults results = (MultipleTaskResults) outputEvent;
					for (TaskResult result : results.getResults()) {

						List<SystemEvent> jobEvents = new ArrayList<>(sourceEvents);
						jobEvents.add(result.getEvent());

						if (result.isPublishable()) {
							eventService.publishEvent(result.getEvent());
						}

						processResult(trigger, triggeredEvent, result, jobEvents);
					}

				} else {

					sourceEvents.add(outputEvent.getEvent());

					if (outputEvent.isPublishable()) {
						eventService.publishEvent(outputEvent.getEvent());
					}

					processResult(trigger, triggeredEvent, outputEvent, sourceEvents);
				}
			}
		} catch (ResourceException | AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	protected void processResult(TriggerResource trigger, SystemEvent triggeredEvent, TaskResult result, List<SystemEvent> sourceEvents)
			throws ValidationException {

		for(SystemEvent event : sourceEvents) {
			if (event instanceof TaskResultCallback) {
				TaskResultCallback callbackEvent = (TaskResultCallback) event;
				try {
					callbackEvent.processResult(result);
				} catch (Throwable e) {
				}
			}
		}

		if (!trigger.getChildTriggers().isEmpty()) {
			for (TriggerResource t : trigger.getChildTriggers()) {
				processEventTrigger(t, result.getEvent(), new ArrayList<SystemEvent>(sourceEvents));
			}

		}
	}
}
