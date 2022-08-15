package com.hypersocket.profile.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileCreationJob extends PermissionsAwareJob {

	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private ProfileCredentialsService profileService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long principalId = context.getTrigger().getJobDataMap().getLong("targetPrincipalId");
		
		try {
			Principal target = realmService.getPrincipalById(principalId);
			if(target==null) {
				throw new JobExecutionException(String.format("Invalid principal id %d", principalId));
			}
			profileService.createProfile(target, new AuthenticationModulesOperationContext());
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
