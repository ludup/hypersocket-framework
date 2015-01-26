package com.hypersocket.automation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;

public class DailySchedulerJob extends PermissionsAwareJob {

	public DailySchedulerJob() {
	}

	@Autowired
	AutomationResourceService automationService; 
	
	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
	
		automationService.scheduleDailyJobs();
	}

}
