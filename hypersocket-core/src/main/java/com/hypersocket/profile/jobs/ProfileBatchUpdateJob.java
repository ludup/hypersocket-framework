package com.hypersocket.profile.jobs;

import java.util.Arrays;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.Profile;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.profile.ProfileRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileBatchUpdateJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(ProfileBatchUpdateJob.class);
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private ProfileCredentialsService profileService; 
	
	@Autowired
	private ProfileRepository repository;

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		try {
			
			var ctx = new AuthenticationModulesOperationContext();
			for(Profile profile : repository.getProfilesWithStatus(Arrays.asList(getCurrentRealm()))) {
				profileService.updateProfile(realmService.getPrincipalById(profile.getId()), ctx);
			}
			
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
