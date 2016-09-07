package com.hypersocket.account.linking.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.account.linking.AccountLinkingRules;
import com.hypersocket.account.linking.AccountLinkingService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public class SecondaryUserDeletionEventJob extends AbstractSecondaryUserUnlinkingJob {

	static Logger log = LoggerFactory.getLogger(SecondaryUserDeletionEventJob.class);
	
	@Autowired
	AccountLinkingService linkingService;
	
	@Autowired
	RealmService realmService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long principalId = context.getTrigger().getJobDataMap().getLong("principalId");
		Long realmId = context.getTrigger().getJobDataMap().getLong("realmId");
		
		try {
			Realm realm = realmService.getRealmById(realmId);
			
			Principal secondaryPrincipal = realmService.getDeletedPrincipalById(realm, principalId, PrincipalType.USER);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Secondary account %s has been deleted", secondaryPrincipal.getPrincipalName()));
			}
			
			unlinkAccount(secondaryPrincipal, realm);
		
		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
	}

}
