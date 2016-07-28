package com.hypersocket.attributes.user;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.TransactionOperation;

public interface UserAttributeRepository extends AbstractAssignableResourceRepository<UserAttribute> {

	Long getMaximumAttributeWeight(UserAttributeCategory cat);

	UserAttribute getAttributeByVariableName(String attributeName, Realm realm);

	void deleteResource(UserAttribute resource, @SuppressWarnings("unchecked") TransactionOperation<UserAttribute>... ops) throws ResourceChangeException;

}
