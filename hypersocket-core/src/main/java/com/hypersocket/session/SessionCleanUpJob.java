package com.hypersocket.session;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class SessionCleanUpJob extends PermissionsAwareJob {

	private final static Logger log = LoggerFactory.getLogger(SessionCleanUpJob.class);
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) {
		try(var c = sessionService.tryWithElevatedPermissions(SystemPermission.SYSTEM)) {
			sessionService.cleanUp();
		} catch (IOException | AccessDeniedException e) {
			log.error("Access Denied", e);
		}
	}

	@Override
	protected void onTransactionComplete() {
		if(log.isDebugEnabled()) {
			log.debug("Completed session clean up job");
		}
		
	}

	@Override
	protected void onTransactionFailure(Throwable t) {
		log.error("Session clean up job failed", t);
		
	}

}
