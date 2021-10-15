package com.hypersocket.realm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hypersocket.cache.CacheService;
import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.delegation.UserDelegationResourceService;
import com.hypersocket.delegation.events.UserDelegationResourceEvent;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.events.GroupEvent;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.HibernateUtils;
import com.hypersocket.role.events.RoleEvent;

@Component
public class DelegationCriteria implements CriteriaConfiguration {

	@Autowired
	private UserDelegationResourceService delegationService;
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private CacheService cacheService;
	
	@Override
	public void configure(Criteria criteria) {
		
		if(!realmService.hasAuthenticatedContext()) {
			return;
		}
		
		/**
		 * This used to include roles in the query but it would not work with groups. We now
		 * resolve all roles down to principals, and groups principals down to user principals.
		 * 
		 * We will apply a limit of 1000 user principals to make this manageable.
		 */
		Principal currentUser = realmService.getCurrentPrincipal();
		
		if(currentUser.isSystem() || permissionService.hasAdministrativePermission(currentUser)) {
			return;
		}
		
		@SuppressWarnings("rawtypes")
		Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
		
		String key = String.format("%s/%s", 
				realmService.getCurrentRealm().getUuid(),
				currentUser.getUUID());
		
		if(!cachedUserIds.containsKey(key)) {
			
			int maximumUsers = Integer.parseInt(System.getProperty("hypersocket.maximumUserDelegates", "1000"));
			
			Collection<UserDelegationResource> resources = delegationService.getPersonalResources();
			Set<Principal> delegatedPrincipals = new HashSet<>();
			Set<Role> allUserRoles = permissionService.getAllUserRoles();
			boolean everyone = false;
			for(UserDelegationResource resource : resources) {
				if(!Collections.disjoint(resource.getRoleDelegates(), allUserRoles)) {
					everyone = true;
					break;
				}
				
				Set<Principal> processed = new HashSet<>();
				for(Role role : resource.getRoleDelegates()) {
					
					for(Principal principal : role.getPrincipals()) {
						if(principal.getType()==PrincipalType.GROUP) {
							iterateGroupMembership(principal, delegatedPrincipals, processed);
						} else {
							delegatedPrincipals.add(principal);
						}
						if(delegatedPrincipals.size() > maximumUsers) {
							throw new IllegalStateException("Too many user delegates for principal query");
						}
					}
				}
				
				delegatedPrincipals.addAll(resource.getUserDelegates());
				
				if(delegatedPrincipals.size() > maximumUsers) {
					throw new IllegalStateException("Too many user delegates for principal query");
				}
				
				processed.clear();
				for(Principal principal : resource.getGroupDelegates()) {
					iterateGroupMembership(principal, delegatedPrincipals, processed);
					if(delegatedPrincipals.size() > maximumUsers) {
						throw new IllegalStateException("Too many user delegates for principal query");
					}
				}
			}
			
			if(everyone) {
				return;
			}
			
			cachedUserIds.put(key, HibernateUtils.getResourceIds(delegatedPrincipals));
		
		}
		
		@SuppressWarnings("unchecked")
		Collection<Long> userIds =  (Collection<Long>) cachedUserIds.get(key);
			
		if(userIds != null && !userIds.isEmpty()) {
			criteria.add(Restrictions.in("id", userIds));
		}
	}

	private void iterateGroupMembership(Principal principal, Set<Principal> delegatedPrincipals,
			Set<Principal> processed) {
		if(processed.contains(principal)) {
			return;
		}
		processed.add(principal);
		delegatedPrincipals.addAll(realmService.getGroupUsers(principal));
		for(Principal assosciated : realmService.getGroupGroups(principal)) {
			iterateGroupMembership(assosciated, delegatedPrincipals, processed);
		}
	}

	@EventListener
	public void onRoleChange(RoleEvent evt) {
		if(evt.isSuccess()) {
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedRoleIds = cacheService.getCacheOrCreate("delegationQueryRoleIds", String.class, Collection.class);
			cachedRoleIds.removeAll();
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
			cachedUserIds.removeAll();
		}
	}
	
	@EventListener
	public void onGroupChange(GroupEvent evt) {
		if(evt.isSuccess()) {
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
			cachedUserIds.removeAll();
		}
	}
	
	@EventListener
	public void onDelegationChange(UserDelegationResourceEvent evt) {
		if(evt.isSuccess()) {
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
			cachedUserIds.removeAll();
		}
	}
}
