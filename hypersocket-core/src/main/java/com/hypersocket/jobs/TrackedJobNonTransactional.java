package com.hypersocket.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public abstract class TrackedJobNonTransactional extends PermissionsAwareJobNonTransactional {

	@Autowired
	JobResourceService jobService; 
	
	TrackedJobData data;
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		data = (TrackedJobData) context.getTrigger().getJobDataMap();
		
		try {
			jobService.reportJobStarting(data.getUUID());
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
			jobService.reportJobComplete(data.getUUID(), getResult());
		} catch (ResourceException | InvalidJobStateException e) {
		}
	}


	@Override
	protected void onJobError(Throwable t) {
		try {
			if(jobService.isJobActive(data.getUUID())) {
				jobService.reportJobFailed(data.getUUID(), t);
			}
		} catch (ResourceException | InvalidJobStateException e) {
		}
	}
	
	protected abstract void onExecuteJob(TrackedJobData data) throws JobExecutionException;

	protected abstract String getResult();
	
	protected void reportJobException(Throwable t) {
		onJobError(t);
	}
	
	protected void reportFailedJob(String result) throws ResourceException, InvalidJobStateException {
		jobService.reportJobFailed(data.getUUID(), result);
	}
}
