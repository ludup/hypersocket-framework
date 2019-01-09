package com.hypersocket.change;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AssignableResource;

public interface ResourceAssignmentChangeListener<T extends AssignableResource> {

	AbstractAssignableResourceRepository<T> getRepository();

	Class<? extends AssignableResource> getResourceClass();

	void principalUnassigned(Realm realm, AssignableResource resource, Iterable<Principal> principals);

	void principalAssigned(Realm realm, AssignableResource resource, Iterable<Principal> principals);
}
