package com.hypersocket.realm;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.scheduler.PermissionsAwareJobData;

public class ResumeUserJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(ResumeUserJob.class);

	@Autowired
	PrincipalSuspensionService suspensionService;

	@Autowired
	RealmService realmService; 
	
	public ResumeUserJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		PermissionsAwareJobData data = (PermissionsAwareJobData) context.getTrigger().getJobDataMap();
		
		String name = (String) data.get("name");

		if (name == null) {
			throw new JobExecutionException(
					"ResumeUserJob job requires name parameter!");
		}

		if (log.isInfoEnabled()) {
			log.info("Resuming user " + name.toString());
		}

		Principal principal = realmService.getPrincipalByName(
				data.getCurrentRealm(), name, PrincipalType.USER);

		if (principal != null) {
			suspensionService.deletePrincipalSuspension(principal);

			suspensionService.notifyResume(name, true);

			if (log.isInfoEnabled()) {
				log.info("Resumed user " + name.toString());
			}
		}
	}

}
