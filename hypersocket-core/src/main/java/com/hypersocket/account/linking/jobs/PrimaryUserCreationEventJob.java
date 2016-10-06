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

public class PrimaryUserCreationEventJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(PrimaryUserCreationEventJob.class);
	
	@Autowired
	AccountLinkingService linkingService;
	
	@Autowired
	RealmService realmService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long principalId = context.getTrigger().getJobDataMap().getLong("principalId");
		Long primaryRealmId = context.getTrigger().getJobDataMap().getLong("realmId");
		Long secondaryRealmId = context.getTrigger().getJobDataMap().getLong("secondaryRealmId");
		
		try {
			Realm primaryRealm = realmService.getRealmById(primaryRealmId);
			Realm secondaryRealm = realmService.getRealmById(secondaryRealmId);
			
			Principal primaryPrincipal = realmService.getPrincipalById(primaryRealm, principalId, PrincipalType.USER);
			
			AccountLinkingRules rules = linkingService.getSecondaryRules(secondaryRealm);
			
			String secondaryUsername = rules.generateSecondaryPrincipalName(primaryPrincipal);
			
			Principal secondaryPrincipal = realmService.getPrincipalByName(rules.getSecondaryRealm(), secondaryUsername, PrincipalType.USER);
			
			if(secondaryPrincipal==null) {
				
				if(log.isInfoEnabled()) {
					log.info(String.format("There is no account to link in %s for %s", 
							rules.getSecondaryRealm().getName(),
							secondaryUsername));
				}
				
				if(!rules.isCreationEnabled()) {
					if(log.isInfoEnabled()) {
						log.info(String.format("Account creation IS NOT supported for %s", secondaryRealm.getName()));
					}
					return;
				}
				
				if(!rules.isAccountCreationRequired(primaryPrincipal)) {
					if(log.isInfoEnabled()) {
						log.info(String.format("Account creation IS NOT required for %s/%s", 
								secondaryRealm.getName(),
								secondaryUsername));
					}
					return;
				}
				
				if(log.isInfoEnabled()) {
					log.info(String.format("Creating secondary principal %s/%s", 
							secondaryRealm.getName(), 
							secondaryUsername));
				}
				
				secondaryPrincipal = rules.createSecondaryPrincipal(primaryPrincipal);
			
			}

			if(log.isInfoEnabled()) {
				log.info(String.format("Account %s/%s has been created and linked to %s/%s", 
						secondaryRealm.getName(),
						secondaryPrincipal.getPrincipalName(),
						primaryRealm.getName(),
						primaryPrincipal.getPrincipalName()));
			}

		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
		
	}

}
