package com.hypersocket.realm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.delegation.UserDelegationResourceService;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.HibernateUtils;

@Component
public class DelegationCriteria implements CriteriaConfiguration {

	@Autowired
	private UserDelegationResourceService delegationService;
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Override
	public void configure(Criteria criteria) {
		
		
		Collection<UserDelegationResource> resources = delegationService.getPersonalResources();
		Set<Role> delegatedRoles = new HashSet<>();
		Set<Principal> delegatedPrincipals = new HashSet<>();
		Set<Role> allUserRoles = permissionService.getAllUserRoles();
		boolean everyone = false;
		for(UserDelegationResource resource : resources) {
			if(!Collections.disjoint(resource.getRoleDelegates(), allUserRoles)) {
				everyone = true;
				break;
			}
			delegatedRoles.addAll(resource.getRoleDelegates());
			for(Principal principal : resource.getUserDelegates()) {
				delegatedPrincipals.add(principal);
			}
			for(Principal principal : resource.getGroupDelegates()) {
				
				for(Principal assosciated : realmService.getAssociatedPrincipals(principal)) {
					if(assosciated.getType()==PrincipalType.USER) {
						delegatedPrincipals.add(assosciated);
					} else if(assosciated.getType() == PrincipalType.GROUP) {
						delegatedPrincipals.addAll(realmService.getAssociatedPrincipals(assosciated, PrincipalType.USER));
					}
				}
			}
		}
		
		if(everyone) {
			return;
		}
		
		if(!delegatedRoles.isEmpty()) {
			criteria.createAlias("roles", "delegated");
			criteria.add(Restrictions.in("delegated.id", HibernateUtils.getResourceIds(delegatedRoles)));
		}
		
		if(!delegatedPrincipals.isEmpty()) {
			criteria.add(Restrictions.in("id", HibernateUtils.getResourceIds(delegatedPrincipals)));
		}
	}

}
