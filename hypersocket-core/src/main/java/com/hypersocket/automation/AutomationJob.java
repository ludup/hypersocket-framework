package com.hypersocket.automation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class AutomationJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(AutomationJob.class);
	
	@Autowired
	AutomationResourceService automationService; 
	
	public AutomationJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		
		
		Long resourceId = context.getTrigger().getJobDataMap().getLong("resourceId");
		
		try {
			AutomationResource resource = automationService.getResourceById(resourceId);
			
			AutomationProvider provider = automationService.getAutomationProvider(resource);
			
			provider.performTask(resource);
		} catch (ResourceNotFoundException e) {
			log.error("Could not find resource id " + resourceId + " to execute job", e);
		}
	}

}
