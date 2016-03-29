package com.hypersocket.feedback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;

@Service
public class FeedbackServiceImpl extends PasswordEnabledAuthenticatedServiceImpl implements FeedbackService {
	
	@Autowired
	SchedulerService schedulerService;
	
	Map<String,FeedbackProgress> progressByUUID = new HashMap<String,FeedbackProgress>();
	
	@Override
	public void startJob(FeedbackProgress progress, Class<? extends FeedbackEnabledJob> jobClz, PermissionsAwareJobData data, String jobResourceKey) throws SchedulerException {
		
		data.put(FeedbackEnabledJob.FEEDBACK_ITEM, progress.getUuid());
		schedulerService.scheduleNow(jobClz, data);
	}

	@Override
	public FeedbackProgress createFeedbackProgress() {
		FeedbackProgress progress = new FeedbackProgress();
		progress.init(UUID.randomUUID().toString(), this);
		progressByUUID.put(progress.getUuid(), progress);
		return progress;
	}
	
	@Override
	public FeedbackProgress getFeedbackProgress(String uuid) {
		return progressByUUID.get(uuid);
	}
	
	@Override
	public void closeFeedbackProgress(FeedbackProgress progress) {
		progressByUUID.remove(progress.getUuid());
	}
	
}
