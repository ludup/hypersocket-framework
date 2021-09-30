package com.hypersocket.realm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hypersocket.cache.CacheService;
import com.hypersocket.delegation.UserDelegationResource;
import com.hypersocket.delegation.UserDelegationResourceService;
import com.hypersocket.delegation.events.UserDelegationResourceEvent;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
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
		 * This used ot incliude roles in the query but it would not work with groups. We now
		 * resolve all roles down to principals, and groups principals down to user principals.
		 * 
		 * We will apply a limit of 1000 user principals to make this managable.
		 */
		Principal currentUser = realmService.getCurrentPrincipal();
		
		if(currentUser.isSystem() || permissionService.hasAdministrativePermission(currentUser)) {
			return;
		}
		
//		@SuppressWarnings("rawtypes")
//		Cache<String,Collection> cachedRoleIds = cacheService.getCacheOrCreate("delegationQueryRoleIds", String.class, Collection.class);
		@SuppressWarnings("rawtypes")
		Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
		
		String key = String.format("%s/%s", 
				realmService.getCurrentRealm().getUuid(),
				currentUser.getUUID());
		
		if(!cachedUserIds.containsKey(key)) {
			
			int maximumRoles = Integer.parseInt(System.getProperty("hypersocket.maximumRoleDelegates", "100"));
			int maximumUsers = Integer.parseInt(System.getProperty("hypersocket.maximumUserDelegates", "1000"));
			
			Collection<UserDelegationResource> resources = delegationService.getPersonalResources();
//			Set<Role> delegatedRoles = new HashSet<>();
			Set<Principal> delegatedPrincipals = new HashSet<>();
			Set<Role> allUserRoles = permissionService.getAllUserRoles();
			boolean everyone = false;
			for(UserDelegationResource resource : resources) {
				if(!Collections.disjoint(resource.getRoleDelegates(), allUserRoles)) {
					everyone = true;
					break;
				}
				
				
				/**
				 * TODO we will only resolve users of the role this way. What about groups?
				 */
				
				for(Role role : resource.getRoleDelegates()) {
					
					for(Principal principal : role.getPrincipals()) {
						for(Principal assosciated : realmService.getAssociatedPrincipals(principal)) {
							if(assosciated.getType()==PrincipalType.USER) {
								delegatedPrincipals.add(assosciated);
							} else if(assosciated.getType() == PrincipalType.GROUP) {
								delegatedPrincipals.addAll(realmService.getAssociatedPrincipals(assosciated, PrincipalType.USER));
							}
						}
						
						if(delegatedPrincipals.size() > maximumUsers) {
							throw new IllegalStateException("Too many user delegates for principal query");
						}
					}
				}
//				if(delegatedRoles.size() > maximumRoles) {
//					throw new IllegalStateException("Too many role delegates for principal query");
//				}
				
				delegatedPrincipals.addAll(resource.getUserDelegates());
				
				if(delegatedPrincipals.size() > maximumUsers) {
					throw new IllegalStateException("Too many user delegates for principal query");
				}
				
				for(Principal principal : resource.getGroupDelegates()) {
					
					for(Principal assosciated : realmService.getAssociatedPrincipals(principal)) {
						if(assosciated.getType()==PrincipalType.USER) {
							delegatedPrincipals.add(assosciated);
						} else if(assosciated.getType() == PrincipalType.GROUP) {
							delegatedPrincipals.addAll(realmService.getAssociatedPrincipals(assosciated, PrincipalType.USER));
						}
					}
					
					if(delegatedPrincipals.size() > maximumUsers) {
						throw new IllegalStateException("Too many user delegates for principal query");
					}
				}
			}
			
			if(everyone) {
				return;
			}
			
//			cachedRoleIds.put(key, HibernateUtils.getResourceIds(delegatedRoles));
			cachedUserIds.put(key, HibernateUtils.getResourceIds(delegatedPrincipals));
		
		}
		
//		@SuppressWarnings("unchecked")
//		Collection<Long> roleIds =  (Collection<Long>) cachedRoleIds.get(key);
		@SuppressWarnings("unchecked")
		Collection<Long> userIds =  (Collection<Long>) cachedUserIds.get(key);
		
//		if(roleIds != null && !roleIds.isEmpty()) {
//			criteria.createAlias("roles", "delegated", JoinType.LEFT_OUTER_JOIN);
//
//			if(userIds!=null && !userIds.isEmpty()) {
//				criteria.add(Restrictions.or(Restrictions.in("delegated.id", roleIds),
//						Restrictions.in("id", userIds)));
//			} else {
//				criteria.add(Restrictions.in("delegated.id", roleIds));
//			}
//		} else 
			
		if(userIds != null && !userIds.isEmpty()) {
			criteria.add(Restrictions.in("id", userIds));
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
	public void onDelegationChange(UserDelegationResourceEvent evt) {
		if(evt.isSuccess()) {
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedRoleIds = cacheService.getCacheOrCreate("delegationQueryRoleIds", String.class, Collection.class);
			cachedRoleIds.removeAll();
			@SuppressWarnings("rawtypes")
			Cache<String,Collection> cachedUserIds = cacheService.getCacheOrCreate("delegationQueryUserIds", String.class, Collection.class);
			cachedUserIds.removeAll();
		}
	}
}
