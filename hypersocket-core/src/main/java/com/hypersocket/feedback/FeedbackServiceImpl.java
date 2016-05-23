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
	
	Map<String,FeedbackProgress> feedbacks = new HashMap<String,FeedbackProgress>();
	
	@Override
	public FeedbackProgress startJob( Class<? extends FeedbackEnabledJob> jobClz, PermissionsAwareJobData data, String jobResourceKey) throws SchedulerException {
		
		FeedbackProgress progress = createFeedbackProgress();
		data.put(FeedbackEnabledJob.FEEDBACK_ITEM, progress.getUuid());
		
		schedulerService.scheduleNow(jobClz, data);
		
		return progress;
	}

	@Override
	public FeedbackProgress createFeedbackProgress() {
		FeedbackProgress progress = new FeedbackProgress();
		String uuid = UUID.randomUUID().toString();
		progress.init(uuid, this);
		feedbacks.put(uuid, progress);
		return progress;
	}

	@Override
	public FeedbackProgress getFeedbackProgress(String uuid) {
		return feedbacks.get(uuid);
	}

	@Override
	public void closeFeedbackProgress(FeedbackProgress progress) {
		feedbacks.remove(progress.getUuid());
		
	}

	
}
