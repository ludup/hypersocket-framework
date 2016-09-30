package com.hypersocket.account.linking.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public class BulkSecondaryUserUnlinkingJob extends AbstractSecondaryUserUnlinkingJob {

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long primaryRealmId = context.getTrigger().getJobDataMap().getLong("primaryRealmId");
		Long secondaryRealmId = context.getTrigger().getJobDataMap().getLong("secondaryRealmId");
		Boolean disableAccount = context.getTrigger().getJobDataMap().getBoolean("disableAccount");
		try {
			
			Realm primaryRealm = realmService.getRealmById(primaryRealmId);
			Realm secondaryRealm = realmService.getRealmById(secondaryRealmId);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Executing bulk unlinking operation for realm %s to %s", 
						primaryRealm.getName(),
						secondaryRealm.getName()));
			}
			
			for(Principal secondaryPrincipal : realmService.allUsers(secondaryRealm)) {
				
				if(log.isInfoEnabled()) {
					log.info(String.format("Looking for primary account linked to %s", secondaryPrincipal.getName()));
				}
				unlinkAccount(secondaryPrincipal, primaryRealm, disableAccount);
			}
			
		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
		
	}

}
