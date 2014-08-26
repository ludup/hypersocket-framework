package com.hypersocket.triggers;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessEventTriggersJob extends AbstractTriggerJob implements Job {

	@Autowired
	TriggerResourceRepository repository;
	
//	@Override
//	public void onExecute(JobExecutionContext context)
//			throws JobExecutionException {
//		
//		SystemEvent event = (SystemEvent) context.getTrigger()
//				.getJobDataMap().get("event");
//		TriggerResource trigger = (TriggerResource) context.getTrigger().getJobDataMap().get("trigger");
//		
//		try {
//			processEventTrigger(trigger, event);
//			// TODO successful event
//		} catch (TriggerValidationException e) {
//			// TDOO failure event
//			e.printStackTrace();
//		}
//		
//		
//	}

}
