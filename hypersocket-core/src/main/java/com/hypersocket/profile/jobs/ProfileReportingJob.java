package com.hypersocket.profile.jobs;

import java.util.Arrays;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.profile.ProfileHistoryRepository;
import com.hypersocket.profile.ProfileRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class ProfileReportingJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(ProfileReportingJob.class);
	
	@Autowired
	private ProfileRepository repository;
	
	@Autowired
	private ProfileHistoryRepository historyRepository;
	
	@Autowired
	private RealmRepository realmRepository; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
			
		if(log.isInfoEnabled()) {
			log.info("Updating profile history");
		}
		
		for(Realm realm : realmRepository.allRealms()) {
		
			if(!realm.isSystem()) {
				long realmCount = repository.getCompleteProfileCount(Arrays.asList(realm));
				historyRepository.report(realm, realmCount);	
				
				if(log.isInfoEnabled()) {
					log.info("Realm {} has a total profile count of {}", realm.getName(), realmCount);
				}
			}
			
		}
		
		long systemCount = repository.getCompleteProfileCount(null);
		historyRepository.report(getCurrentRealm(), systemCount);
			
		if(log.isInfoEnabled()) {
			log.info("System has a total combined profile count of {}", systemCount);
		}

	}

}
