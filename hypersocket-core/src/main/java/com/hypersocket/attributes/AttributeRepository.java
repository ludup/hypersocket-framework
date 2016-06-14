package com.hypersocket.attributes;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;

public interface AttributeRepository<A extends AbstractAttribute<?>, C extends RealmAttributeCategory<?>> extends AbstractAssignableResourceRepository<A> {

	Long getMaximumAttributeWeight(C cat);

	A getAttributeByVariableName(String attributeName, Realm realm);

}
