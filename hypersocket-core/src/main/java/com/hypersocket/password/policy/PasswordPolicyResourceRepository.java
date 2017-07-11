package com.hypersocket.password.policy;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceRepository;

public interface PasswordPolicyResourceRepository extends
		AbstractAssignableResourceRepository<PasswordPolicyResource> {

	PasswordPolicyResource getPolicyByDN(String dn, Realm realm);

	PasswordPolicyResource getDefaultPolicyByModule(Realm realm, String moduleName);

}
