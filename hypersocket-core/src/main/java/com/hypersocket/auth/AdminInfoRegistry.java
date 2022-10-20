package com.hypersocket.auth;

import java.util.Optional;
import java.util.Set;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;

public interface AdminInfoRegistry {

	Set<Role> allAdminRoles(Realm realm);
	
	Optional<String> adminAuthenticationSchemeName(Realm realm);
	
	Optional<AuthenticationScheme> adminAuthenticationScheme(Realm realm);
}
