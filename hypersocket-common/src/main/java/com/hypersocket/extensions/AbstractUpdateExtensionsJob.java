package com.hypersocket.extensions;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUpdateExtensionsJob extends AbstractExtensionUpdater implements Job {

	static Logger log = LoggerFactory.getLogger(AbstractUpdateExtensionsJob.class);

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		onInitUpdate(context);
		try {
			if(!update()) {
				throw new JobExecutionException("Nothing to update.");
			}
		} catch (IOException ioe) {
			throw new JobExecutionException("Update job failed.", ioe);
		}
	}
	
	protected abstract void onInitUpdate(JobExecutionContext context) throws JobExecutionException;
	

}
