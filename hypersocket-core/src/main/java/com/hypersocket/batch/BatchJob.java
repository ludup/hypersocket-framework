package com.hypersocket.batch;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public class BatchJob extends PermissionsAwareJobNonTransactional {

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		@SuppressWarnings("unchecked")
		Class<? extends BatchProcessingService<?>> clz = (Class<? extends BatchProcessingService<?>>)context.getTrigger().getJobDataMap().get("clz");
		BatchProcessingService<?> service = ApplicationContextServiceImpl.getInstance().getBean(clz);
		service.processBatchItems();
	}

}
