package com.hypersocket.attributes;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.TransactionOperation;

public interface AttributeRepository<A extends AbstractAttribute<?>, C extends RealmAttributeCategory<?>> extends AbstractAssignableResourceRepository<A> {

	Long getMaximumAttributeWeight(C cat);

	A getAttributeByVariableName(String attributeName, Realm realm);

	void deleteResource(A resource, @SuppressWarnings("unchecked") TransactionOperation<A>... ops) throws ResourceChangeException;

}
