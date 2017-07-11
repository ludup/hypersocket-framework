package com.hypersocket.password.policy;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public interface PolicyResolver {

	PasswordPolicyResource resolvePrincipalPasswordPolicy(Principal principal);
	
	PasswordPolicyResource getDefaultPasswordPolicy(Realm realm);
}
