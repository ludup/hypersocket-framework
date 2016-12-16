package com.hypersocket.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.PermissionsAwareJob;

public abstract class TrackedJob extends PermissionsAwareJob {

	@Autowired
	JobResourceService jobService; 
	
	JobDataMap data;
	String uuid = null;
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		data = context.getTrigger().getJobDataMap();
		uuid = data.getString("trackingUUID");
		try {
			if(uuid!=null) {
				jobService.reportJobStarting(uuid);
			}
			onExecuteJob(data);
			onJobComplete();
		} catch (Throwable t) {
			onJobError(t);
			throw new JobExecutionException(t);
		}
		
	}
	
	protected void onJobComplete() {
		if(uuid!=null) {
			try {
				jobService.reportJobComplete(uuid, getResult());
			} catch (ResourceException | InvalidJobStateException e) {
			}
		}
	}


	protected void onJobError(Throwable t) {
		if(uuid!=null) {
			try {
				if(jobService.isJobActive(uuid)) {
					jobService.reportJobFailed(uuid, t);
				}
			} catch (ResourceException | InvalidJobStateException e) {
			}
		}
	}
	
	protected abstract void onExecuteJob(JobDataMap data) throws JobExecutionException;

	protected abstract String getResult();
	
	protected void reportJobException(Throwable t) {
		onJobError(t);
	}
	
	protected void reportFailedJob(String result) throws ResourceException, InvalidJobStateException {
		if(uuid!=null) {
			jobService.reportJobFailed(uuid, result);
		}
	}
}
