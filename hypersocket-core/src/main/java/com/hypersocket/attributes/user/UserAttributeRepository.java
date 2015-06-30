package com.hypersocket.attributes.user;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;

public interface UserAttributeRepository extends AbstractAssignableResourceRepository<UserAttribute> {

	Long getMaximumAttributeWeight(UserAttributeCategory cat);

	UserAttribute getAttributeByVariableName(String attributeName, Realm realm);

}
