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

public class PrimaryUserDeletionEventJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(PrimaryUserDeletionEventJob.class);
	
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
			
			Principal primaryPrincipal = realmService.getDeletedPrincipalById(realm, principalId, PrincipalType.USER);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Primary account %s has been deleted", primaryPrincipal.getPrincipalName()));
			}
			
			for(AccountLinkingRules rules : linkingService.getPrimaryRules(realm)) {
			
				if(linkingService.hasLinkedAccount(rules.getSecondaryRealm(), primaryPrincipal)) {
					Principal secondaryPrincipal = linkingService.getLinkedAccount(
							rules.getSecondaryRealm(), primaryPrincipal);
					
					if(log.isInfoEnabled()) {
						log.info(String.format("Unlinking account %s from %s", primaryPrincipal.getPrincipalName(), 
								secondaryPrincipal.getPrincipalName()));
					}
					
					linkingService.unlinkAccounts(primaryPrincipal, secondaryPrincipal);
					if(rules.isAutomaticLinking() && rules.isDeletionEnabled()) {
						
						if(log.isInfoEnabled()) {
							log.info(String.format("Deleting account %s", 
									secondaryPrincipal.getPrincipalName()));
						}
						
						realmService.deleteUser(secondaryPrincipal.getRealm(), secondaryPrincipal);
					}
				}
			}
		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
	}

}
