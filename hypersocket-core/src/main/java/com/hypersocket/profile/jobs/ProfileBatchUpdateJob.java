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
import com.hypersocket.profile.ProfileHistoryRepository;
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
	
	@Autowired
	private ProfileHistoryRepository historyRepository;
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		try {
			
			var ctx = new AuthenticationModulesOperationContext();
			for(Profile profile : repository.getProfilesWithStatus(Arrays.asList(getCurrentRealm()))) {
				profileService.updateProfile(realmService.getPrincipalById(profile.getId()), ctx);
			}
			
			if(log.isInfoEnabled()) {
				log.info("Updating profile history");
			}
			
			if(getCurrentRealm().isSystem()) {
				long systemCount = repository.getCompleteProfileCount(null);
				historyRepository.report(getCurrentRealm(), systemCount);
				
				if(log.isInfoEnabled()) {
					log.info("System has a total combined profile count of {}", systemCount);
				}
			} else {
				long realmCount = repository.getCompleteProfileCount(Arrays.asList(getCurrentRealm()));
				historyRepository.report(getCurrentRealm(), realmCount);	
				
				if(log.isInfoEnabled()) {
					log.info("Realm {} has a total profile count of {}", getCurrentRealm().getName(), realmCount);
				}
			}
			
		} catch (AccessDeniedException e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
	}

}
