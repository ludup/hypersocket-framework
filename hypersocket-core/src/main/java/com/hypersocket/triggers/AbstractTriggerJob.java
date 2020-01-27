package com.hypersocket.triggers;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public abstract class AbstractTriggerJob extends PermissionsAwareJobNonTransactional {

	@Autowired
	private TriggerExecutor triggerExecutor; 
	
	static Logger log = LoggerFactory.getLogger(AbstractTriggerJob.class);

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
	
	protected void processEventTrigger(TriggerResource trigger,
			SystemEvent event, List<SystemEvent> sourceEvents) throws ValidationException {
		triggerExecutor.processEventTrigger(trigger, event, sourceEvents);
	}
}
