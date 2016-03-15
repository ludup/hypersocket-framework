package com.hypersocket.feedback;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hypersocket.scheduler.PermissionsAwareJob;

public abstract class FeedbackEnabledJob extends PermissionsAwareJob {

	public static final String FEEDBACK_ITEM = "feedback";
	
	FeedbackProgress progress;
	
	public FeedbackEnabledJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		FeedbackProgress progress = (FeedbackProgress) context.getTrigger().getJobDataMap().get(FEEDBACK_ITEM);
		startJob(context, progress);
	}
	
	protected abstract void startJob(JobExecutionContext context, FeedbackProgress progress) throws JobExecutionException;

	protected void onTransactionComplete() {
		progress.complete(getCompleteResourceKey());
	}

	protected void onTransactionFailure(Throwable t) {
		progress.failed(getFailedResourceKey(), t);
	}

	protected String getCompleteResourceKey() {
		return "jobComplete.text";
	}
	
	protected String getFailedResourceKey() {
		return "jobFailed.text";
	}
}
