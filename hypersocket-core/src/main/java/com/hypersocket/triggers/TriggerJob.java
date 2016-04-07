package com.hypersocket.triggers;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.events.TriggerExecutedEvent;

public class TriggerJob extends AbstractTriggerJob {

	
	@Override
	public void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		SystemEvent source = (SystemEvent) context.getTrigger().getJobDataMap().get("sourceEvent");
		SystemEvent event = (SystemEvent) context.getTrigger().getJobDataMap().get("event");
		
		if(log.isInfoEnabled()) {
			log.info("Starting trigger job for event " + event.getResourceKey());
		}
	
		TriggerResource trigger = (TriggerResource) context.getTrigger()
				.getJobDataMap().get("trigger");

		try {
			processEventTrigger(trigger, event, source);
		} catch (Throwable e) {
			eventService.publishEvent(new TriggerExecutedEvent(this, trigger, e));
		} 

	}

}
