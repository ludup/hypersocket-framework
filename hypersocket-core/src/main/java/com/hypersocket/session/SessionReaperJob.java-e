package com.hypersocket.session;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class SessionReaperJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(SessionReaperJob.class);
	
	@Autowired
	SessionService sessionService;
	
	@Autowired
	RealmService realmService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) {

		try {
			List<Session> activeSessions = sessionService.getActiveSessions();
			
			if(log.isDebugEnabled()) {
				log.debug("There are " + activeSessions.size() + " users connected");
			}
			
			boolean firstRun = context.getTrigger().getJobDataMap().containsKey("firstRun");
			
			for(Session session : activeSessions) {
				if(!session.isSystem()) {
					if(sessionService.isLoggedOn(session, false)) {
						if(firstRun) {
							if(realmService.getRealmPropertyBoolean(session.getPrincipalRealm(), "session.closeOnShutdown")) {
								sessionService.closeSession(session);
							}
						}
					}
				}
			}
		} catch (AccessDeniedException e) {
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
