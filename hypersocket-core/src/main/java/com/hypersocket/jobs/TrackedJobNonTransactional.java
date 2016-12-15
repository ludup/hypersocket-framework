package com.hypersocket.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public abstract class TrackedJobNonTransactional extends PermissionsAwareJobNonTransactional {

	@Autowired
	JobResourceService jobService; 
	
	JobDataMap data;
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		data = context.getTrigger().getJobDataMap();
		
		try {
			jobService.reportJobStarting(data.getString("uuid"));
			onExecuteJob(data);
			onJobComplete();
		} catch (Throwable t) {
			onJobError(t);
			throw new JobExecutionException(t);
		}
		
	}
	
	@Override
	protected void onJobComplete() {
		try {
			jobService.reportJobComplete(data.getString("uuid"), getResult());
		} catch (ResourceException | InvalidJobStateException e) {
		}
	}


	@Override
	protected void onJobError(Throwable t) {
		try {
			if(jobService.isJobActive(data.getString("uuid"))) {
				jobService.reportJobFailed(data.getString("uuid"), t);
			}
		} catch (ResourceException | InvalidJobStateException e) {
		}
	}
	
	protected abstract void onExecuteJob(JobDataMap data) throws JobExecutionException;

	protected abstract String getResult();
	
	protected void reportJobException(Throwable t) {
		onJobError(t);
	}
	
	protected void reportFailedJob(String result) throws ResourceException, InvalidJobStateException {
		jobService.reportJobFailed(data.getString("uuid"), result);
	}
}
