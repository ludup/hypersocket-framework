package com.hypersocket.change;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
import com.hypersocket.util.Iterators;
import com.hypersocket.util.ProxiedIterator;
import com.hypersocket.util.SingleItemIterator;

@Service
public class ResourceAssignmentChangeServiceImpl implements ResourceAssignmentChangeService {

	static Logger log = LoggerFactory.getLogger(ResourceAssignmentChangeServiceImpl.class);

	Map<Class<? extends AssignableResource>, ResourceAssignmentChangeListener<?>> listeners = new HashMap<Class<? extends AssignableResource>, ResourceAssignmentChangeListener<?>>();

	@Autowired
	private RealmService realmService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Override
	public synchronized void addListener(ResourceAssignmentChangeListener<?> listener) {
		listeners.put(listener.getResourceClass(), listener);
	}

	@EventListener
	public synchronized void handleResourceEvent(AssignableResourceEvent event) {
		if (event.isSuccess() && listeners.containsKey(event.getResource().getClass())) {
			TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
			template.execute((status) -> {
			
				Set<Principal> assigned = new HashSet<Principal>();
				Set<Principal> unassigned = new HashSet<Principal>();
				boolean assignedEveryone = false;
				boolean unassignedEveryone = false;
				for (Role role : event.getAssignedRoles()) {
					if (role.isAllUsers()) {
						assignedEveryone = true;
						assigned.clear();
						break;
					}
					assigned.addAll(role.getPrincipals());
				}
	
				if (assignedEveryone) {
					processEveryoneAssignment(event.getCurrentRealm(), (AssignableResource) event.getResource());
				} else {
					
					for (Role role : event.getUnassignedRoles()) {
						if (role.isAllUsers()) {
							unassignedEveryone = true;
							unassigned.clear();
							break;
						}
						unassigned.addAll(role.getPrincipals());
					}
	
					assigned = Iterators.setFromIteration(permissionService.resolveUsers(assigned.iterator()));
					unassigned = Iterators.setFromIteration(permissionService.resolveUsers(unassigned.iterator()));
					unassigned.removeAll(assigned);
	
					for (Role r : ((AssignableResource) event.getResource()).getRoles()) {
						if (event.getAssignedRoles().contains(r) || event.getUnassignedRoles().contains(r)) {
							continue;
						}
						if (r.isAllUsers()) {
							assigned.clear();
							unassigned.clear();
							assignedEveryone = false;
							unassignedEveryone = false;
							break;
						}
						Set<Principal> tmp = Iterators.setFromIteration(permissionService.resolveUsers(r.getPrincipals().iterator()));
						assigned.removeAll(tmp);
						unassigned.removeAll(tmp);
						if (assigned.isEmpty() && unassigned.isEmpty()) {
							break;
						}
					}
	
					if (unassignedEveryone) {
						processEveryoneUnassignment(event.getCurrentRealm(), (AssignableResource) event.getResource(),
								assigned);
					} else {
						if (!assigned.isEmpty() || !unassigned.isEmpty()) {
							processAssignmentEvent(event.getCurrentRealm(), (AssignableResource) event.getResource(),
									assigned, unassigned);
						}
					}
				}
				return null;
			});
		}
	}

	@EventListener
	public synchronized void handleRoleEvent(RoleUpdatedEvent event) {

		if (event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), event.getGranted(), event.getRevoked());
		}
	}

	@EventListener
	public synchronized void handleRoleEvent(RoleCreatedEvent event) {

		if (event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), event.getRole().getPrincipals(),
					Collections.<Principal>emptySet());
		}
	}

	@EventListener
	public synchronized void handleRoleEvent(RoleDeletedEvent event) {

		if (event.isSuccess()) {
			processRoleEvent(event.getCurrentRealm(), event.getRole(), Collections.<Principal>emptySet(),
					event.getRole().getPrincipals());
		}
	}

	private void processRoleEvent(Realm realm, Role role, Collection<Principal> granted,
			Collection<Principal> revoked) {
		if (!listeners.isEmpty()) {
			TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
			template.execute((status) -> {

				Set<Principal> assigned = Iterators.setFromIteration(permissionService.resolveUsers(granted.iterator()));
				Set<Principal> unassigned = Iterators.setFromIteration(permissionService.resolveUsers(revoked.iterator()));
				unassigned.removeAll(assigned);
	
				if (!assigned.isEmpty() || !unassigned.isEmpty()) {
					for (ResourceAssignmentChangeListener<?> listener : listeners.values()) {
						long resources = listener.getRepository().getResourceByRoleCount(realm, role);
						if (resources > 0) {
							for (AssignableResource resource : listener.getRepository().getResourcesByRole(realm, role)) {
								for (Role r : resource.getRoles()) {
									if (r.equals(role)) {
										continue;
									}
									if (r.isAllUsers()) {
										assigned.clear();
										unassigned.clear();
										break;
									}
									Set<Principal> tmp = Iterators.setFromIteration(permissionService.resolveUsers(r.getPrincipals().iterator()));
									assigned.removeAll(tmp);
									unassigned.removeAll(tmp);
									if (assigned.isEmpty() && unassigned.isEmpty()) {
										break;
									}
								}
								if (!assigned.isEmpty() || !unassigned.isEmpty()) {
									processAssignmentEvent(realm, resource, assigned, unassigned);
								}
							}
						}
					}
				}
				return null;
			});
		}
	}

	@EventListener
	public synchronized void handleGroupEvent(GroupUpdatedEvent event) {
		if (event.isSuccess()) {
			processGroupEvent(event.getCurrentRealm(), event.getTargetPrincipal(), event.getGranted(),
					event.getRevoked());
		}
	}

	@EventListener
	public synchronized void handleGroupEvent(GroupDeletedEvent event) {
		if (event.isSuccess()) {
			processGroupEvent(event.getCurrentRealm(), event.getTargetPrincipal(), Collections.<Principal>emptySet(),
					event.getAssosicatedPrincipals());
		}
	}

	private void processGroupEvent(Realm realm, Principal group, Collection<Principal> granted,
			Collection<Principal> revoked) {
		if (!listeners.isEmpty()) {

			TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
			template.execute((status) -> {

				Set<Principal> assigned = Iterators.setFromIteration(permissionService.resolveUsers(granted.iterator()));
				Set<Principal> unassigned = Iterators.setFromIteration(permissionService.resolveUsers(revoked.iterator()));
				unassigned.removeAll(assigned);
	
				if (!assigned.isEmpty() || !unassigned.isEmpty()) {
					Collection<Role> roles = permissionService.getRolesByPrincipal(group);
	
					for (ResourceAssignmentChangeListener<?> listener : listeners.values()) {
						long resources = listener.getRepository().getResourceByRoleCount(realm, roles.toArray(new Role[0]));
						if (resources > 0) {
							for (AssignableResource resource : listener.getRepository().getResourcesByRole(realm,
									roles.toArray(new Role[0]))) {
								for (Role r : resource.getRoles()) {
									if (r.isAllUsers()) {
										assigned.clear();
										unassigned.clear();
										break;
									}
									Set<Principal> tmp = Iterators.setFromIteration(permissionService.resolveUsers(r.getPrincipals().iterator()));
									assigned.removeAll(tmp);
									unassigned.removeAll(tmp);
									if (assigned.isEmpty() && unassigned.isEmpty()) {
										break;
									}
								}
								if (!assigned.isEmpty() || !unassigned.isEmpty()) {
									processAssignmentEvent(realm, resource, assigned, unassigned);
								}
							}
						}
					}
	
				}
				return null;
			});
		}
	}

	private void processAssignmentEvent(Realm realm, AssignableResource resource, Iterable<Principal> granted,
			Iterable<Principal> revoked) {

		ResourceAssignmentChangeListener<?> listener = listeners.get(resource.getClass());

		if (listener != null) {
			// We now have a definitive list of the changes for this resource
			listener.principalAssigned(realm, resource, granted);
			listener.principalUnassigned(realm, resource, revoked);
		}
	}

	protected void processEveryoneAssignment(Realm realm, AssignableResource resource) {

		processAssignmentEvent(realm, resource, new Iterable<Principal>() {
			@Override
			public Iterator<Principal> iterator() {
				return new ProxiedIterator<Principal>() {
					Iterator<Principal> alreadyAssignedIt;

					{
						try {
							alreadyAssignedIt = permissionService.iteratePrincipalsByRole(realm,
									resource.getRoles().toArray(new Role[0]));
						} catch (Exception e) {
							throw new IllegalStateException(
									String.format("Failed to iterate principals bys role %s", resource.getRoles()), e);
						}
					}

					@Override
					protected Principal checkNext(Principal principal) {
						if (principal == null) {
							while (alreadyAssignedIt.hasNext()) {
								principal = alreadyAssignedIt.next();

								boolean assigned = false;
								for (Iterator<Principal> resolvedIt = permissionService
										.resolveUsers(new SingleItemIterator<>(principal)); resolvedIt.hasNext();) {
									Principal resolvedPrincipal = resolvedIt.next();
									if (resolvedPrincipal.equals(principal)) {
										assigned = true;
										break;
									}
								}

								if (!assigned)
									break;
								else
									principal = null;
							}
						}
						return principal;
					}
				};
			}
		}, Collections.emptyList());

	}

	protected void processEveryoneUnassignment(Realm realm, AssignableResource resource,
			Collection<Principal> keepAssigned) {

		// Everyone was unassigned but some users were assigned too.
		processAssignmentEvent(realm, resource, Collections.emptyList(), new Iterable<Principal>() {
			@Override
			public Iterator<Principal> iterator() {
				return new ProxiedIterator<Principal>() {

					Iterator<Principal> principalIt = realmService.iterateUsers(realm);
					
					protected Principal checkNext(Principal principal) {
						if (principal == null) {
							while(principalIt.hasNext()) {
								principal = principalIt.next();
								if(!keepAssigned.contains(principal))
									break;
								else
									principal = null;
							}
						}
						return principal;
					}
				};
			}
		});
	}
}
