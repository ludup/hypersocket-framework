/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.JobData;
import com.hypersocket.session.SessionReaperJob;
import com.hypersocket.session.SessionService;


public class core_1_DOT_3_DOT_0 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_1_DOT_3_DOT_0.class);

	@Autowired
	private ClusteredSchedulerService schedulerService;
	
	@Autowired
	private SessionService sessionService;
	
	@Override
	public void run() {

		if (log.isInfoEnabled()) {
			log.info("Scheduling session reaper job");
		}
		
		try {
			if(schedulerService.jobDoesNotExists("firstRunSessionReaperJob")){
				var session = sessionService.getSystemSession();
				try(var c = sessionService.tryAs(session, session.getCurrentRealm(), session.getCurrentPrincipal(), null)) {
					schedulerService.scheduleNow(SessionReaperJob.class, "firstRunSessionReaperJob", JobData.of(
						"firstRunSessionReaperJob",
						"firstRun", true
					));
				}
			}

		} catch (Exception e) {
			log.error("Failed to schedule session reaper job", e);
		} 
		
	}


}
