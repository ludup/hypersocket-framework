package com.hypersocket.attributes.user;

import com.hypersocket.attributes.AttributeRepository;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;

public interface UserAttributeRepository extends AttributeRepository<UserAttribute, UserAttributeCategory> {

	void deleteResource(UserAttribute resource, @SuppressWarnings("unchecked") TransactionOperation<UserAttribute>... ops) throws ResourceException;

}
