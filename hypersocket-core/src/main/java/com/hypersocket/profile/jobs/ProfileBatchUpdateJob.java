package com.hypersocket.profile.jobs;

import java.util.Iterator;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileCredentialsService;
import com.hypersocket.profile.ProfileRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileBatchUpdateJob extends PermissionsAwareJob {

	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private ProfileCredentialsService profileService; 
	
	@Autowired
	private ProfileRepository repository;
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		try {
			Iterator<Principal> it = realmService.iterateUsers(getCurrentRealm());
			while(it.hasNext()) {
				profileService.updateProfile(it.next());
			}
			
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
