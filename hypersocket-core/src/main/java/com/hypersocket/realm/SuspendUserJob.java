package com.hypersocket.realm;

import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;

public class SuspendUserJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(SuspendUserJob.class);

	@Autowired
	private PrincipalSuspensionService suspensionService;
	
	@Autowired
	private RealmService realmService; 
	
	public SuspendUserJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		try {
			PrincipalSuspensionServiceImpl principalSuspensionServiceImpl = (PrincipalSuspensionServiceImpl) suspensionService;
			
			JobDataMap data = context.getTrigger().getJobDataMap();
			
			String name = (String) data.get("name");
			long duration = (long) data.get("duration");

			if (name == null) {
				throw new JobExecutionException(
						"ResumeUserJob job requires name parameter!");
			}
			
			principalSuspensionServiceImpl.clearExistingJob(name, getCurrentRealm());

			if (log.isInfoEnabled()) {
				log.info("Suspending user " + name.toString());
			}
			
			Principal principal = realmService.getPrincipalByName(
					getCurrentRealm(), name, PrincipalType.USER);

			if (principal != null) {
				principalSuspensionServiceImpl.actOnSuspensionNow(principal, name, realm, new Date(), duration);
			}
		} catch (JobExecutionException e) {
			log.error("Job failed", e);
		}
	}
}
