package com.hypersocket.scheduler;

import org.quartz.JobDataMap;

public interface AllowOneJobConcurrently {
	
	String jobId(JobDataMap jobDataMap);
}
