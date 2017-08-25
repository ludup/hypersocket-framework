package com.hypersocket.profile.jobs;

import java.util.Arrays;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.Profile;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.profile.ProfileRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileBatchUpdateJob extends PermissionsAwareJob {

	@Autowired
	RealmService realmService; 
	
	@Autowired
	ProfileCredentialsService profileService; 
	
	@Autowired
	ProfileRepository repository;
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		try {
			for(Profile profile : repository.getProfilesWithStatus(Arrays.asList(getCurrentRealm()))) {
				profileService.updateProfile(realmService.getPrincipalById(profile.getId()));
			}
			
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
