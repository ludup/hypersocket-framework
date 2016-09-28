package com.hypersocket.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpJob implements Job{

    static Logger log = LoggerFactory.getLogger(NoOpJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(log.isDebugEnabled()){
            log.debug("Executing no op job.");
        }
    }
}
