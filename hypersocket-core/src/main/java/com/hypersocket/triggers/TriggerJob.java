package com.hypersocket.triggers;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.events.TriggerExecutedEvent;

public class TriggerJob extends AbstractTriggerJob {
	
	@Autowired
	private EventService eventService;
	
	@Override
	public void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		@SuppressWarnings("unchecked")
		List<SystemEvent> source = (List<SystemEvent>) context.getTrigger().getJobDataMap().get("sourceEvent");
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
