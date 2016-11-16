package com.hypersocket.feedback;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public abstract class FeedbackEnabledNoTransactionalJob extends PermissionsAwareJobNonTransactional implements FeedbackJob {

	public static final String FEEDBACK_ITEM = "feedback";
	
	@Autowired
	FeedbackService feedbackService; 
	
	protected FeedbackProgress progress;
	
	public FeedbackEnabledNoTransactionalJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		try {
			String uuid = (String) context.getTrigger().getJobDataMap().get(FEEDBACK_ITEM);
			if(uuid==null) {
				progress = feedbackService.createFeedbackProgress();
			} else {
				progress = feedbackService.getFeedbackProgress(uuid);
			}
			startJob(context, progress);
			progress.complete(getCompleteResourceKey());
		} catch (Throwable t) {
			progress.failed(getFailedResourceKey(), t, t.getCause()!=null ? t.getCause().getMessage() : t.getMessage());
			throw t;
		}
	}
	
	protected abstract void startJob(JobExecutionContext context, FeedbackProgress progress) throws JobExecutionException;

	protected String getCompleteResourceKey() {
		return "jobComplete.text";
	}
	
	protected String getFailedResourceKey() {
		return "jobFailed.text";
	}
}
