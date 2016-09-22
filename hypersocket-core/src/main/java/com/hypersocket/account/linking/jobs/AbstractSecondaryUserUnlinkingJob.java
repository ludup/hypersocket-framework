package com.hypersocket.account.linking.jobs;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.account.linking.AccountLinkingService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.PermissionsAwareJob;

public abstract class AbstractSecondaryUserUnlinkingJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(AbstractSecondaryUserUnlinkingJob.class);
	
	@Autowired
	AccountLinkingService linkingService;
	
	@Autowired
	RealmService realmService; 
	

	protected void unlinkAccount(Principal secondaryPrincipal, Realm primaryRealm) throws JobExecutionException {
		
		try {

			if(secondaryPrincipal.getParentPrincipal()!=null) {
					
				if(log.isInfoEnabled()) {
					log.info(String.format("Unlinking account %s from %s", secondaryPrincipal.getParentPrincipal().getPrincipalName(), 
							secondaryPrincipal.getPrincipalName()));
				}
				
				linkingService.unlinkAccounts(secondaryPrincipal.getParentPrincipal(), secondaryPrincipal);
				
			}

		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
	}

}
