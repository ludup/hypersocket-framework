package com.hypersocket.change;

import java.util.Collection;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.AssignableResource;

public interface ResourceAssignmentChangeListener<T extends AssignableResource> {

	AbstractAssignableResourceRepository<T> getRepository();

	Class<? extends AssignableResource> getResourceClass();

	void principalUnassigned(Realm realm, AssignableResource resource, Collection<Principal> principals);

	void principalAssigned(Realm realm, AssignableResource resource, Collection<Principal> principals);
}
