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

public class BulkRoleUnassignmentUserUnlinkingJob extends AbstractSecondaryUserUnlinkingJob {

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		Long primaryRealmId = context.getTrigger().getJobDataMap().getLong("primaryRealmId");
		String roleName = context.getTrigger().getJobDataMap().getString("roleName");
		Long[] ids = (Long[]) context.getTrigger().getJobDataMap().get("unassignedIds");
		
		try {
			
			Realm primaryRealm = realmService.getRealmById(primaryRealmId);
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Checking unassignments to role %s to determine if accounts need to be unlinked", 
						roleName));
			}
			
			Set<Principal> processedPrincipals = new HashSet<Principal>();
			
			for(AccountLinkingRules rules : linkingService.getPrimaryRules(primaryRealm)) {
				if(rules.isAutomaticLinking() && rules.isAssignmentEnabled()) {
					
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
			log.info(String.format("Processing principal unassignment %s for secondary realm %s", 
					primaryPrincipal.getPrincipalName(), 
					rules.getSecondaryRealm().getName()));
		}
		Principal secondaryPrincipal;
		
		if(!rules.isSecondaryAccountRequired(primaryPrincipal) && rules.isSecondaryAccountAvailable(primaryPrincipal)) {
			secondaryPrincipal = rules.getSecondaryPrincipal(primaryPrincipal);
			if(secondaryPrincipal.isLinked()) {
				if(!secondaryPrincipal.getParentPrincipal().equals(primaryPrincipal)) {
					if(log.isInfoEnabled()) {
						log.info(String.format("Secondary principal %s/%s is linked but not to %s", secondaryPrincipal.getRealm().getName(),
								secondaryPrincipal.getPrincipalName(), primaryPrincipal.getName()));
					}
					return;
				}
				unlinkAccount(secondaryPrincipal, primaryPrincipal.getRealm(), rules.isDisableAccountRequired());				
			}
		} 
	}

}
