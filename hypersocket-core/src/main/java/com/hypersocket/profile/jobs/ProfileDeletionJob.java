package com.hypersocket.profile.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileDeletionJob extends PermissionsAwareJob {

	@Autowired
	RealmService realmService; 
	
	@Autowired
	ProfileCredentialsService profileService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long principalId = context.getJobDetail().getJobDataMap().getLong("targetPrincipalId");
		
		try {
			Principal target = realmService.getPrincipalById(principalId);
			if(target==null) {
				throw new JobExecutionException(String.format("Invalid principal id %d", principalId));
			}
			profileService.deleteProfile(target);
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
