package com.hypersocket.realm;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ResumeUserJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(ResumeUserJob.class);

	@Autowired
	RealmService service;

	public ResumeUserJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {

		String name = (String) context.getTrigger().getJobDataMap().get("name");

		if (name == null) {
			throw new JobExecutionException(
					"ResumeUserJob job requires name parameter!");
		}

		try {
			if (log.isInfoEnabled()) {
				log.info("Resuming user " + name.toString());
			}

			service.deletePrincipalSuspension(service.getUniquePrincipal(name));

			service.notifyResume(name, true);

			if (log.isInfoEnabled()) {
				log.info("Resumed user " + name.toString());
			}

		} catch (ResourceNotFoundException e) {
			log.error("Failed to resume user " + name.toString());
		}

	}

}
