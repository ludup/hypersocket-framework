package com.hypersocket.session;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class SessionCleanUpJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(SessionCleanUpJob.class);
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) {

		sessionService.elevatePermissions(SystemPermission.SYSTEM);
		try {
			sessionService.cleanUp();
		} catch (AccessDeniedException e) {
			log.error("Access Denied", e);
		} finally {
			sessionService.clearElevatedPermissions();
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
