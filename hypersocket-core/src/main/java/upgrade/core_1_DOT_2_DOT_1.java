/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.session.SessionReaperJob;

public class core_1_DOT_2_DOT_1 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_1_DOT_2_DOT_1.class);

	@Autowired
	SchedulerService schedulerService;
	
	@Override
	public void run() {

		if (log.isInfoEnabled()) {
			log.info("Scheduling session reaper job");
		}

		try {
			JobDataMap data = new JobDataMap();
			data.put("jobName", "firstRunSessionReaperJob");
			data.put("identity", "firstRunSessionReaperJob");
			data.put("firstRun", true);
			
			// If exists/....
			
			schedulerService.scheduleNow(SessionReaperJob.class, data);

		} catch (SchedulerException e) {
			log.error("Failed to schedule session reaper job", e);
		} 
		
	}


}
