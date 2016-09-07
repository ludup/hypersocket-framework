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

public class BulkRoleAssignmentUserLinkingJob extends AbstractSecondaryUserLinkingJob {

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long primaryRealmId = context.getTrigger().getJobDataMap().getLong("primaryRealmId");
		String roleName = context.getTrigger().getJobDataMap().getString("roleName");
		
		Long[] ids = (Long[]) context.getTrigger().getJobDataMap().get("assignedIds");
		
		try {
			
			Realm primaryRealm = realmService.getRealmById(primaryRealmId);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Checking assignments to role %s to determine if accounts need to be linked", 
						roleName));
			}
			
			Set<Principal> processedPrincipals = new HashSet<Principal>();
			
			for(AccountLinkingRules rules : linkingService.getPrimaryRules(primaryRealm)) {
				if(rules.isAutomaticLinking() && rules.isAccountLinkedToRole()) {
					
					for(Long id : ids) {
						
						Principal primaryPrincipal = realmService.getPrincipalById(
								primaryRealm, 
								id, 
								PrincipalType.USER, PrincipalType.GROUP);
						
						switch(primaryPrincipal.getType()) {
						case USER:
							processPrincipal(rules, primaryPrincipal, processedPrincipals);
							break;
						case GROUP:
							processGroupPrincipals(primaryPrincipal, rules, processedPrincipals);
							break;
						default:
							break;
						}
					}
				}
			}
			
			
			
		} catch(Throwable t) {
			throw new IllegalStateException(t.getMessage(), t);
		}
		
	}
	
	private void processGroupPrincipals(Principal primaryPrincipal, AccountLinkingRules rules,
			Set<Principal> processedPrincipals) throws ResourceCreationException, JobExecutionException, AccessDeniedException {
		
		if(processedPrincipals.contains(primaryPrincipal)) {
			return;
		}
		processedPrincipals.add(primaryPrincipal);
		
		for(Principal principal : realmService.getAssociatedPrincipals(primaryPrincipal)) {
			switch(principal.getType()) {
			case USER:
				processPrincipal(rules, principal, processedPrincipals);
			case GROUP:
				processGroupPrincipals(principal, rules, processedPrincipals);
				break;
			default:
				break;
			}
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
