package com.hypersocket.session;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.scheduler.TransactionalJob;

public class SessionReaperJob extends TransactionalJob {

	static Logger log = LoggerFactory.getLogger(SessionReaperJob.class);
	
	@Autowired
	SessionService sessionService;
	
	@Override
	protected Object onExecute(JobExecutionContext context) {
		
		
		try {
			List<Session> activeSessions = sessionService.getActiveSessions();
			
			if(log.isDebugEnabled()) {
				log.debug("There are " + activeSessions.size() + " users connected");
			}
			for(Session session : activeSessions) {
				if(!session.system) {
					sessionService.isLoggedOn(session, false);
				}
			}
		} catch (AccessDeniedException e) {
			log.error("Access Denied", e);
		}
		
		return null;
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
