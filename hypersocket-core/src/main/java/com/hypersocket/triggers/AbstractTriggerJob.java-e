package com.hypersocket.triggers;

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
import com.hypersocket.tasks.TaskProviderService;

public abstract class AbstractTriggerJob extends PermissionsAwareJob {

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
	TriggerExecutor triggerExecutor; 
	
	static Logger log = LoggerFactory.getLogger(AbstractTriggerJob.class);

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
	
	protected void processEventTrigger(TriggerResource trigger,
			SystemEvent event, SystemEvent originalEvent) throws ValidationException {
		triggerExecutor.processEventTrigger(trigger, event, originalEvent);
	}
}
