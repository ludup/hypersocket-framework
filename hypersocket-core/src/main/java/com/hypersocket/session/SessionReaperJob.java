package com.hypersocket.session;

import java.io.IOException;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class SessionReaperJob extends PermissionsAwareJob {

	private final static Logger log = LoggerFactory.getLogger(SessionReaperJob.class);
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) {
		try(var c = sessionService.tryWithElevatedPermissions(SystemPermission.SYSTEM)) {
			List<Session> activeSessions = sessionService.getActiveSessions();
			
			if(log.isDebugEnabled()) {
				log.debug("There are " + activeSessions.size() + " users connected");
			}
			
			for(Session session : activeSessions) {
				if(!session.isSystem()) {
					if(sessionService.isLoggedOn(session, false)) {
						sessionService.notifyReaperListeners(session);
					}
				}
			}
		} catch (IOException | AccessDeniedException e) {
			log.error("Access Denied", e);
		} 
	}

	@Override
	protected void onTransactionComplete() {
		if(log.isDebugEnabled()) {
			log.debug("Completed session reaper job");
		}
		
	}

	@Override
	protected void onTransactionFailure(Throwable t) {
		log.error("Session reaper job failed", t);
		
	}

}
