package com.hypersocket.change;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.events.GroupDeletedEvent;
import com.hypersocket.realm.events.GroupUpdatedEvent;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.role.events.RoleCreatedEvent;
import com.hypersocket.role.events.RoleDeletedEvent;
import com.hypersocket.role.events.RoleUpdatedEvent;

@Service
public class ResourceAssignmentChangeServiceImpl implements ResourceAssignmentChangeService {

	static Logger log = LoggerFactory.getLogger(ResourceAssignmentChangeServiceImpl.class);
	
	Map<Class<? extends AssignableResource>, ResourceAssignmentChangeListener<?>> listeners = 
			new HashMap<Class<? extends AssignableResource>, ResourceAssignmentChangeListener<?>>();
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	PermissionService permissionService; 
	
	@Override
	public synchronized void addListener(ResourceAssignmentChangeListener<?> listener) {
		listeners.put(listener.getResourceClass(), listener);	
	}
	
	@EventListener
	public synchronized void handleResourceEvent(AssignableResourceEvent event) {
		if(event.isSuccess() && listeners.containsKey(event.getResource().getClass())) {
			Set<Principal> assigned = new HashSet<Principal>();
			Set<Principal> unassigned = new HashSet<Principal>();
			boolean assignedEveryone = false;
			boolean unassignedEveryone = false;
			for(Role role : event.getAssignedRoles()) {
				if(role.isAllUsers()) {
					assignedEveryone = true;
					assigned.clear();
					break;
				}
				assigned.addAll(role.getPrincipals());
			}
			
			for(Role role : event.getUnassignedRoles()) {
				if(role.isAllUsers()) {
					unassignedEveryone = true;
					unassigned.clear();
					break;
				}
				unassigned.addAll(role.getPrincipals());
			}
			
			assigned = resolveUsers(assigned);
			unassigned = resolveUsers(unassigned);
			unassigned.removeAll(assigned);
			
			for(Role r : ((AssignableResource)event.getResource()).getRoles()) {
				if(event.getAssignedRoles().contains(r) || event.getUnassignedRoles().contains(r)) {
					continue;
				}
				if(r.isAllUsers()) {
					assigned.clear();
					unassigned.clear();
					assignedEveryone = false;
					unassignedEveryone = false;
					break;
				}
				Set<Principal> tmp = resolveUsers(r.getPrincipals());
				assigned.removeAll(tmp);
				unassigned.removeAll(tmp);
				if(assigned.isEmpty() && unassigned.isEmpty()) {
					break;
				}
			}
			
			if(assignedEveryone) {
				processEveryoneAssignment(event.getCurrentRealm(), (AssignableResource) event.getResource());
			} else {
				if(unassignedEveryone) {
					processEveryoneUnassignment(event.getCurrentRealm(), (AssignableResource) event.getResource(), assigned);
				} else {
					if(!assigned.isEmpty() || !unassigned.isEmpty()) {
						processAssignmentEvent(event.getCurrentRealm(), (AssignableResource) event.getResource(), assigned, unassigned);
					}
				}
			}
		}
	}

	

	@EventListener
	public synchronized void handleRoleEvent(RoleUpdatedEvent event) {
	
		if(event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), event.getGranted(), event.getRevoked());
		}
	}
	
	@EventListener
	public synchronized void handleRoleEvent(RoleCreatedEvent event) {
	
		if(event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), event.getRole().getPrincipals(), Collections.<Principal>emptySet());
		}
	}
	
	@EventListener
	public synchronized void handleRoleEvent(RoleDeletedEvent event) {
		
		if(event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), Collections.<Principal>emptySet(), event.getRole().getPrincipals());
		}
	}
	
	private void processRoleEvent(Realm realm, Role role, Collection<Principal> granted, Collection<Principal> revoked) {
		if(!listeners.isEmpty()) {
			
			Set<Principal> assigned = resolveUsers(granted);
			Set<Principal> unassigned = resolveUsers(revoked);
			unassigned.removeAll(assigned);
			
			if(!assigned.isEmpty() || !unassigned.isEmpty()) {
				for(ResourceAssignmentChangeListener<?> listener : listeners.values()) {
					long resources = listener.getRepository().getResourceByRoleCount(realm, role);
					if(resources > 0) {
						for(AssignableResource resource : listener.getRepository().getResourcesByRole(realm, role)) {
							for(Role r : resource.getRoles()) {
								if(r.equals(role)) {
									continue;
								}
								if(r.isAllUsers()) {
									assigned.clear();
									unassigned.clear();
									break;
								}
								Set<Principal> tmp = resolveUsers(r.getPrincipals());
								assigned.removeAll(tmp);
								unassigned.removeAll(tmp);
								if(assigned.isEmpty() && unassigned.isEmpty()) {
									break;
								}
							}
							if(!assigned.isEmpty() || !unassigned.isEmpty()) {
								processAssignmentEvent(realm, resource, assigned, unassigned);
							}
						}
					}
				}
			}
		}
	}
	
	@EventListener
	public synchronized void handleGroupEvent(GroupUpdatedEvent event) {
		if(event.isSuccess()) {
			processGroupEvent(event.getCurrentRealm(), event.getTargetPrincipal(), event.getGranted(), event.getRevoked());
		}
	}
	
	@EventListener 
	public synchronized void handleGroupEvent(GroupDeletedEvent event) {
		if(event.isSuccess()) {
			processGroupEvent(event.getCurrentRealm(), event.getTargetPrincipal(), Collections.<Principal>emptySet(), event.getAssosicatedPrincipals());
		}
	}
	
	private void processGroupEvent(Realm realm, Principal group, Collection<Principal> granted, Collection<Principal> revoked) {
		if(!listeners.isEmpty()) {
			
			Set<Principal> assigned = resolveUsers(granted);
			Set<Principal> unassigned = resolveUsers(revoked);
			unassigned.removeAll(assigned);
			
			if(!assigned.isEmpty() || !unassigned.isEmpty()) {
				Collection<Role> roles = permissionService.getRolesByPrincipal(group);
				
				for(ResourceAssignmentChangeListener<?> listener : listeners.values()) {
					long resources = listener.getRepository().getResourceByRoleCount(realm, roles.toArray(new Role[0]));
					if(resources > 0) {
						for(AssignableResource resource : listener.getRepository().getResourcesByRole(realm, roles.toArray(new Role[0]))) {
							for(Role r : resource.getRoles()) {
								if(r.isAllUsers()) {
									assigned.clear();
									unassigned.clear();
									break;
								}
								Set<Principal> tmp = resolveUsers(r.getPrincipals());
								assigned.removeAll(tmp);
								unassigned.removeAll(tmp);
								if(assigned.isEmpty() && unassigned.isEmpty()) {
									break;
								}
							}
							if(!assigned.isEmpty() || !unassigned.isEmpty()) {
								processAssignmentEvent(realm, resource, assigned, unassigned);
							}
						}
					}
				}
				
			}
		}
	}
	private Set<Principal> resolveUsers(Collection<Principal> principals) {
		
		Set<Principal> resolved = new HashSet<Principal>();
		Set<Principal> processedGroups = new HashSet<Principal>();
		
		for(Principal principal : principals) {
			switch(principal.getType()) {
			case USER:
				resolved.add(principal);
				break;
			case GROUP:
				resolveGroupUsers(principal, resolved, processedGroups);
				break;
			default:
				// Not processing SYSTEM or SERVICE principals.
			}
		}
		return resolved;
	}
	
	private void resolveGroupUsers(Principal group, Collection<Principal> resolved, Set<Principal> processed) {
		if(!processed.contains(group)) {
			processed.add(group);
			for(Principal principal : realmService.getAssociatedPrincipals(group)) {
				switch(principal.getType()) {
				case USER:
					resolved.add(principal);
					break;
				case GROUP:
					resolveGroupUsers(principal, resolved, processed);
					break;
				default:
					// Not processing SYSTEM or SERVICE principals.
				}
			}
			
		}
	}
	

	private void processAssignmentEvent(Realm realm, AssignableResource resource, Set<Principal> granted, Set<Principal> revoked) {
		
		ResourceAssignmentChangeListener<?> listener = listeners.get(resource.getClass());
		
		if(listener!=null) {
			// We now have a definitive list of the changes for this resource
			for(Principal principal : granted) {
				log.info(String.format("Granted access to %s for resource %s", principal.getPrincipalName(), resource.getName()));
			}
			listener.principalAssigned(realm, resource, granted);
			
			for(Principal principal : revoked) {
				log.info(String.format("Revoked access to %s for resource %s", principal.getPrincipalName(), resource.getName()));
			}
			listener.principalUnassigned(realm, resource, revoked);
		}
	}

	protected void processEveryoneAssignment(Realm realm, AssignableResource resource) {
	
		try {
			Set<Principal> assigned = new HashSet<Principal>();
			assigned.addAll(realmService.allUsers(realm));
			
			Collection<Principal> alreadyAssigned = permissionService.getPrincipalsByRole(resource.getRoles().toArray(new Role[0]));
			assigned.removeAll(resolveUsers(alreadyAssigned));
			
			processAssignmentEvent(realm, resource, assigned, Collections.<Principal>emptySet());

		} catch (AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	protected void processEveryoneUnassignment(Realm realm, AssignableResource resource, Collection<Principal> keepAssigned) {
	
		// Everyone was unassigned but some users were assigned too.
		try {
			Set<Principal> unassigned = new HashSet<Principal>();
			unassigned.addAll(realmService.allUsers(realm));
			unassigned.removeAll(keepAssigned);
			
			processAssignmentEvent(realm, resource, Collections.<Principal>emptySet(), unassigned);

		} catch (AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
