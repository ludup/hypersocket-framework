package com.hypersocket.feedback;

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
	
	@Override
	public void startJob(FeedbackProgress progress, Class<? extends FeedbackEnabledJob> jobClz, PermissionsAwareJobData data, String jobResourceKey) throws SchedulerException {
		
		String uuid = UUID.randomUUID().toString();
		progress.init(uuid, this);
		data.put(FeedbackEnabledJob.FEEDBACK_ITEM, progress);
		
		schedulerService.scheduleNow(jobClz, data);
	}

	
}
