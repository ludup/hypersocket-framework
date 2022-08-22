package com.hypersocket.profile.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.AllowOneJobConcurrently;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileUpdateJob extends PermissionsAwareJob implements AllowOneJobConcurrently {

	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private ProfileCredentialsService profileService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long principalId = getPrincipalId(context.getTrigger().getJobDataMap());
		
		try {
			Principal target = realmService.getPrincipalById(principalId);
			if(target==null) {
				throw new JobExecutionException(String.format("Invalid principal id %d", principalId));
			}
			profileService.updateProfile(target, new AuthenticationModulesOperationContext());
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

	@Override
	public String jobId(JobDataMap jobDataMap) {
		return getJobKey(getPrincipalId(jobDataMap));
	}
	
	private Long getPrincipalId(JobDataMap jobDataMap) {
		return jobDataMap.getLong("targetPrincipalId");
	}
	
	private String getJobKey(Long principalId) {
		return String.format("%s_%s", getClass().getName(), principalId);
	}
}
