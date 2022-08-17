package com.hypersocket.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.hypersocket.realm.Realm;

public class AuthenticationModulesOperationContext {

	private Map<String, AuthenticationScheme> schemeByResourceKey = new HashMap<>();
	private Map<Long, List<AuthenticationModule>> modulesByScheme = new HashMap<>();
	private Map<String, Boolean> authenticatorInUse = new HashMap<>();

	public AuthenticationModulesOperationContext() {
	}

	public boolean isAuthenticatorInUse(Realm realm, String resourceKey, Supplier<Boolean> supplier) {
		var k = realm.getId() + "-" + resourceKey;
		if (authenticatorInUse.containsKey(k)) {
			return authenticatorInUse.get(k);
		} else {
			var s = supplier.get();
			authenticatorInUse.put(k, s);
			return s;
		}
	}
	
	public List<AuthenticationModule> getAuthenticationModulesByScheme(final AuthenticationScheme scheme,
			Supplier<List<AuthenticationModule>> supplier) {
		var k = scheme.getId();
		if (modulesByScheme.containsKey(k)) {
			return modulesByScheme.get(k);
		} else {
			var s = supplier.get();
			modulesByScheme.put(k, s);
			return s;
		}
	}

	public AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey,
			Supplier<AuthenticationScheme> supplier) {
		var k = realm.getId() + "-" + resourceKey;
		if (schemeByResourceKey.containsKey(k)) {
			return schemeByResourceKey.get(k);
		} else {
			var s = supplier.get();
			schemeByResourceKey.put(k, s);
			return s;
		}
	}

}
