package com.hypersocket.account.linking.jobs;

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

public abstract class AbstractSecondaryUserLinkingJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(AbstractSecondaryUserLinkingJob.class);
	
	@Autowired
	AccountLinkingService linkingService;
	
	@Autowired
	RealmService realmService; 
	

	protected void linkAccount(Principal secondaryPrincipal, Realm primaryRealm) throws JobExecutionException {
		
		try {

			AccountLinkingRules rules = linkingService.getSecondaryRules(primaryRealm);
			if(rules==null || !rules.isAutomaticLinking()) {
				
				if(log.isInfoEnabled()) {
					log.info(String.format("Automatic linking IS NOT enabled from realm %s to %s", 
							primaryRealm.getName(), 
							secondaryPrincipal.getRealm().getName()));
				}
				
				return;
			}
			
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Automatic linking is enabled from realm %s to %s", 
						primaryRealm.getName(), 
						secondaryPrincipal.getRealm().getName()));
			}
			
			String principalName = rules.generatePrimaryPrincipalName(secondaryPrincipal);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Looking for primary account named %s", principalName));
			}
			
			Principal primaryPrincipal = realmService.getPrincipalByName(
					primaryRealm, 
					principalName, 
					PrincipalType.USER);
			
			if(primaryPrincipal==null) {
				if(log.isInfoEnabled()) {
					log.info(String.format("There is no primary account for %s", principalName));
				}
				return;
			}
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Primary account found for %s", principalName));
			}
			
			linkingService.linkAccounts(primaryPrincipal, secondaryPrincipal);
			

		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
	}

}
