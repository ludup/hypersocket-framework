package com.hypersocket.account.linking.jobs;

import java.util.HashSet;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hypersocket.account.linking.AccountLinkingRules;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceCreationException;

public class BulkPrincipalAssignmentUserLinkingJob extends AbstractSecondaryUserLinkingJob {

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long primaryRealmId = context.getTrigger().getJobDataMap().getLong("primaryRealmId");
		Long[] ids = (Long[]) context.getTrigger().getJobDataMap().get("assignedIds");
		
		try {
			
			Realm primaryRealm = realmService.getRealmById(primaryRealmId);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Checking assignments in realm %s to determine if accounts need to be linked", 
						primaryRealm.getName()));
			}
			
			Set<Principal> processedPrincipals = new HashSet<Principal>();
			
			for(AccountLinkingRules rules : linkingService.getPrimaryRules(primaryRealm)) {
					
				for(Long id : ids) {
					
					Principal primaryPrincipal = realmService.getPrincipalById(
							primaryRealm, 
							id, 
							PrincipalType.USER, PrincipalType.GROUP);
					
					switch(primaryPrincipal.getType()) {
					case USER:
						processPrincipal(rules, primaryPrincipal, processedPrincipals);
						break;
					default:
						throw new IllegalStateException("Cannot link non-user principal to another principal!");
					}
				}
				
			}
		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
		
	}

	private void processPrincipal(AccountLinkingRules rules, Principal primaryPrincipal, Set<Principal> processedPrincipals) throws ResourceCreationException, AccessDeniedException, JobExecutionException {
		
		if(processedPrincipals.contains(primaryPrincipal)) {
			return;
		}
		
		processedPrincipals.add(primaryPrincipal);
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Processing principal assignment %s for secondary realm %s", 
					primaryPrincipal.getPrincipalName(), 
					rules.getSecondaryRealm().getName()));
		}
		Principal secondaryPrincipal;
		
		if(rules.isSecondaryAccountAvailable(primaryPrincipal)) {
			secondaryPrincipal = rules.getSecondaryPrincipal(primaryPrincipal);
			
			linkAccount(secondaryPrincipal, primaryPrincipal.getRealm());
		} else if(rules.isCreationEnabled() && rules.isAccountCreationRequired(primaryPrincipal)) {
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Account needs to be created for principal %s on secondary realm %s", 
						primaryPrincipal.getPrincipalName(), 
						rules.getSecondaryRealm().getName()));
			}
			
			secondaryPrincipal = rules.createSecondaryPrincipal(primaryPrincipal);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Created account %s/%s and linked to %s", 
						secondaryPrincipal.getRealm().getName(),
						secondaryPrincipal.getPrincipalName(),
						primaryPrincipal.getPrincipalName()));
			}
		}
	}

}
