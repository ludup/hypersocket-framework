package com.hypersocket.realm;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;

public class ResumeUserJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(ResumeUserJob.class);

	@Autowired
	private PrincipalSuspensionService suspensionService;

	@Autowired
	private RealmService realmService; 
	
	public ResumeUserJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap data = context.getTrigger().getJobDataMap();
		
		String name = (String) data.get("name");

		if (name == null) {
			throw new JobExecutionException(
					"ResumeUserJob job requires name parameter!");
		}

		if (log.isInfoEnabled()) {
			log.info("Resuming user " + name.toString());
		}

		Principal principal = realmService.getPrincipalByName(
				getCurrentRealm(), name, PrincipalType.USER);

		if (principal != null) {

			suspensionService.deletePrincipalSuspension(principal, PrincipalSuspensionType.MANUAL);
			
			String scheduleId = context.getJobDetail().getKey().getName();
			
			if (log.isInfoEnabled()) {
				log.info("Notifying resume for job with id " + scheduleId);
			}
			
			suspensionService.notifyResume(scheduleId, name, true);

			if (log.isInfoEnabled()) {
				log.info("Resumed user " + name.toString());
			}
		}
	}

}
